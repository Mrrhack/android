package com.x8bit.bitwarden.ui.platform.feature.rootnav

import android.os.Parcelable
import androidx.lifecycle.viewModelScope
import com.x8bit.bitwarden.data.auth.repository.AuthRepository
import com.x8bit.bitwarden.data.auth.repository.model.UserState
import com.x8bit.bitwarden.data.autofill.fido2.model.Fido2CredentialRequest
import com.x8bit.bitwarden.data.autofill.fido2.util.Fido2CredentialAssertionRequest
import com.x8bit.bitwarden.data.autofill.model.AutofillSaveItem
import com.x8bit.bitwarden.data.autofill.model.AutofillSelectionData
import com.x8bit.bitwarden.data.platform.manager.SpecialCircumstanceManager
import com.x8bit.bitwarden.data.platform.manager.model.SpecialCircumstance
import com.x8bit.bitwarden.ui.platform.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * Manages root level navigation state of the application.
 */
@HiltViewModel
class RootNavViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    specialCircumstanceManager: SpecialCircumstanceManager,
) : BaseViewModel<RootNavState, Unit, RootNavAction>(
    initialState = RootNavState.Splash,
) {
    init {
        combine(
            authRepository
                .userStateFlow,
            specialCircumstanceManager
                .specialCircumstanceStateFlow,
        ) { userState, specialCircumstance ->
            RootNavAction.Internal.UserStateUpdateReceive(
                userState = userState,
                specialCircumstance = specialCircumstance,
            )
        }
            .onEach(::handleAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: RootNavAction) {
        when (action) {
            is RootNavAction.BackStackUpdate -> handleBackStackUpdate()
            is RootNavAction.Internal.UserStateUpdateReceive -> handleUserStateUpdateReceive(action)
        }
    }

    private fun handleBackStackUpdate() {
        authRepository.updateLastActiveTime()
    }

    @Suppress("CyclomaticComplexMethod")
    private fun handleUserStateUpdateReceive(
        action: RootNavAction.Internal.UserStateUpdateReceive,
    ) {
        val userState = action.userState
        val updatedRootNavState = when {
            userState?.activeAccount?.trustedDevice?.isDeviceTrusted == false &&
                !userState.activeAccount.isVaultUnlocked &&
                !userState.activeAccount.hasManualUnlockMechanism -> RootNavState.TrustedDevice

            userState?.activeAccount?.needsMasterPassword == true -> RootNavState.SetPassword

            userState?.activeAccount?.needsPasswordReset == true -> RootNavState.ResetPassword

            userState == null ||
                !userState.activeAccount.isLoggedIn ||
                userState.hasPendingAccountAddition -> RootNavState.Auth

            userState.activeAccount.isVaultUnlocked -> {
                when (val specialCircumstance = action.specialCircumstance) {
                    is SpecialCircumstance.AutofillSave -> {
                        RootNavState.VaultUnlockedForAutofillSave(
                            autofillSaveItem = specialCircumstance.autofillSaveItem,
                        )
                    }

                    is SpecialCircumstance.AutofillSelection -> {
                        RootNavState.VaultUnlockedForAutofillSelection(
                            activeUserId = userState.activeAccount.userId,
                            type = specialCircumstance.autofillSelectionData.type,
                        )
                    }

                    is SpecialCircumstance.ShareNewSend -> RootNavState.VaultUnlockedForNewSend

                    is SpecialCircumstance.PasswordlessRequest -> {
                        RootNavState.VaultUnlockedForAuthRequest
                    }

                    is SpecialCircumstance.Fido2Save -> {
                        RootNavState.VaultUnlockedForFido2Save(
                            activeUserId = userState.activeUserId,
                            fido2CredentialRequest = specialCircumstance.fido2CredentialRequest,
                        )
                    }

                    is SpecialCircumstance.Fido2Assertion -> {
                        RootNavState.VaultUnlockedForFido2Assertion(
                            activeUserId = userState.activeUserId,
                            fido2CredentialAssertionRequest = specialCircumstance.fido2AssertionRequest,
                        )
                    }

                    SpecialCircumstance.GeneratorShortcut,
                    SpecialCircumstance.VaultShortcut,
                    null,
                    -> RootNavState.VaultUnlocked(activeUserId = userState.activeAccount.userId)
                }
            }

            else -> RootNavState.VaultLocked
        }
        mutableStateFlow.update { updatedRootNavState }
    }
}

/**
 * Models root level destinations for the app.
 */
sealed class RootNavState : Parcelable {
    /**
     * App should show auth nav graph.
     */
    @Parcelize
    data object Auth : RootNavState()

    /**
     * App should show reset password graph.
     */
    @Parcelize
    data object ResetPassword : RootNavState()

    /**
     * App should show set password graph.
     */
    @Parcelize
    data object SetPassword : RootNavState()

    /**
     * App should show splash nav graph.
     */
    @Parcelize
    data object Splash : RootNavState()

    /**
     * App should show the trusted device destination.
     */
    @Parcelize
    data object TrustedDevice : RootNavState()

    /**
     * App should show vault locked nav graph.
     */
    @Parcelize
    data object VaultLocked : RootNavState()

    /**
     * App should show vault unlocked nav graph for the given [activeUserId].
     */
    @Parcelize
    data class VaultUnlocked(
        val activeUserId: String,
    ) : RootNavState()

    /**
     * App should show an add item screen for a user to complete the saving of data collected by
     * the autofill framework.
     */
    @Parcelize
    data class VaultUnlockedForAutofillSave(
        val autofillSaveItem: AutofillSaveItem,
    ) : RootNavState()

    /**
     * App should show a selection screen for autofill for an unlocked user.
     */
    @Parcelize
    data class VaultUnlockedForAutofillSelection(
        val activeUserId: String,
        val type: AutofillSelectionData.Type,
    ) : RootNavState()

    /**
     * App should show an add item screen for a user to complete the saving of data collected by
     * the fido2 credential manager framework
     *
     * @param activeUserId ID of the active user. Indirectly used to notify [RootNavViewModel] the
     * active user has changed.
     * @param fido2CredentialRequest System request containing FIDO credential data.
     */
    @Parcelize
    data class VaultUnlockedForFido2Save(
        val activeUserId: String,
        val fido2CredentialRequest: Fido2CredentialRequest,
    ) : RootNavState()

    @Parcelize
    data class VaultUnlockedForFido2Assertion(
        val activeUserId: String,
        val fido2CredentialAssertionRequest: Fido2CredentialAssertionRequest,
    ) : RootNavState()

    /**
     * App should show the new send screen for an unlocked user.
     */
    @Parcelize
    data object VaultUnlockedForNewSend : RootNavState()

    /**
     * App should show the auth confirmation screen for an unlocked user.
     */
    @Parcelize
    data object VaultUnlockedForAuthRequest : RootNavState()
}

/**
 * Models root level navigation actions.
 */
sealed class RootNavAction {

    /**
     * Indicates the backstack has changed.
     */
    data object BackStackUpdate : RootNavAction()

    /**
     * Internal ViewModel actions.
     */
    sealed class Internal {

        /**
         * User state in the repository layer changed.
         */
        data class UserStateUpdateReceive(
            val userState: UserState?,
            val specialCircumstance: SpecialCircumstance?,
        ) : RootNavAction()
    }
}
