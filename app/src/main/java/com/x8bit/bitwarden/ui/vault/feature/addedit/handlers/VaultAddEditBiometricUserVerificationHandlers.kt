package com.x8bit.bitwarden.ui.vault.feature.addedit.handlers

import com.x8bit.bitwarden.ui.vault.feature.addedit.VaultAddEditAction
import com.x8bit.bitwarden.ui.vault.feature.addedit.VaultAddEditViewModel

/**
 * A collection of handler functions specifically tailored for managing action within the context of
 * biometric user verification.
 *
 * @property onBiometricsVerificationSuccess Handles the action when biometric verification is
 * successful.
 * @property onBiometricsVerificationFail Handles the action when biometric verification fails.
 * @property onBiometricsLockOut Handles the action when too many failed verification attempts locks
 * out the user for a period of time.
 */
data class VaultAddEditBiometricUserVerificationHandlers(
    val onBiometricsVerificationSuccess: () -> Unit,
    val onBiometricsLockOut: () -> Unit,
    val onBiometricsVerificationFail: () -> Unit,
    val onBiometricsVerificationCancelled: () -> Unit,
) {
    companion object {

        /**
         * Creates an instance of [VaultAddEditBiometricUserVerificationHandlers] by binding actions
         * to the provided [VaultAddEditViewModel].
         */
        fun create(
            viewModel: VaultAddEditViewModel,
        ): VaultAddEditBiometricUserVerificationHandlers =
            VaultAddEditBiometricUserVerificationHandlers(
                onBiometricsVerificationSuccess = {
                    viewModel.trySendAction(VaultAddEditAction.Common.UserVerificationSuccess)
                },
                onBiometricsVerificationFail = {
                    viewModel.trySendAction(VaultAddEditAction.Common.UserVerificationLockOut)
                },
                onBiometricsLockOut = {
                    viewModel.trySendAction(VaultAddEditAction.Common.UserVerificationFail)
                },
                onBiometricsVerificationCancelled = {
                    viewModel.trySendAction(
                        VaultAddEditAction.Common.UserVerificationCancelled,
                    )
                },
            )
    }
}
