package com.x8bit.bitwarden.data.tools.generator.repository

import com.bitwarden.core.PassphraseGeneratorRequest
import com.bitwarden.core.PasswordGeneratorRequest
import com.bitwarden.core.PasswordHistoryView
import com.x8bit.bitwarden.data.auth.datasource.disk.AuthDiskSource
import com.x8bit.bitwarden.data.platform.manager.dispatcher.DispatcherManager
import com.x8bit.bitwarden.data.platform.repository.model.LocalDataState
import com.x8bit.bitwarden.data.platform.repository.util.observeWhenSubscribedAndLoggedIn
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.GeneratorDiskSource
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.PasswordHistoryDiskSource
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.entity.toPasswordHistory
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.entity.toPasswordHistoryEntity
import com.x8bit.bitwarden.data.tools.generator.datasource.sdk.GeneratorSdkSource
import com.x8bit.bitwarden.data.tools.generator.repository.model.GeneratedPassphraseResult
import com.x8bit.bitwarden.data.tools.generator.repository.model.GeneratedPasswordResult
import com.x8bit.bitwarden.data.tools.generator.repository.model.PasscodeGenerationOptions
import com.x8bit.bitwarden.data.vault.datasource.sdk.VaultSdkSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Singleton

/**
 * Default implementation of [GeneratorRepository].
 */
@Singleton
class GeneratorRepositoryImpl(
    private val generatorSdkSource: GeneratorSdkSource,
    private val generatorDiskSource: GeneratorDiskSource,
    private val authDiskSource: AuthDiskSource,
    private val vaultSdkSource: VaultSdkSource,
    private val passwordHistoryDiskSource: PasswordHistoryDiskSource,
    dispatcherManager: DispatcherManager,
) : GeneratorRepository {

    private val scope = CoroutineScope(dispatcherManager.io)
    private val mutablePasswordHistoryStateFlow =
        MutableStateFlow<LocalDataState<List<PasswordHistoryView>>>(LocalDataState.Loading)

    override val passwordHistoryStateFlow: StateFlow<LocalDataState<List<PasswordHistoryView>>>
        get() = mutablePasswordHistoryStateFlow.asStateFlow()

    init {
        mutablePasswordHistoryStateFlow
            .observeWhenSubscribedAndLoggedIn(authDiskSource.userStateFlow) { activeUserId ->
                observePasswordHistoryForUser(activeUserId)
            }
            .launchIn(scope)
    }

    private fun observePasswordHistoryForUser(
        userId: String,
    ): Flow<Result<List<PasswordHistoryView>>> =
        passwordHistoryDiskSource
            .getPasswordHistoriesForUser(userId)
            .onStart { mutablePasswordHistoryStateFlow.value = LocalDataState.Loading }
            .map { encryptedPasswordHistoryList ->
                val passwordHistories = encryptedPasswordHistoryList.map { it.toPasswordHistory() }
                vaultSdkSource.decryptPasswordHistoryList(passwordHistories)
            }
            .onEach { encryptedPasswordHistoryListResult ->
                mutablePasswordHistoryStateFlow.value = encryptedPasswordHistoryListResult.fold(
                    onSuccess = { LocalDataState.Loaded(it) },
                    onFailure = { LocalDataState.Error(it) },
                )
            }

    override suspend fun generatePassword(
        passwordGeneratorRequest: PasswordGeneratorRequest,
    ): GeneratedPasswordResult =
        generatorSdkSource
            .generatePassword(passwordGeneratorRequest)
            .fold(
                onSuccess = { GeneratedPasswordResult.Success(it) },
                onFailure = { GeneratedPasswordResult.InvalidRequest },
            )

    override suspend fun generatePassphrase(
        passphraseGeneratorRequest: PassphraseGeneratorRequest,
    ): GeneratedPassphraseResult =
        generatorSdkSource
            .generatePassphrase(passphraseGeneratorRequest)
            .fold(
                onSuccess = { GeneratedPassphraseResult.Success(it) },
                onFailure = { GeneratedPassphraseResult.InvalidRequest },
            )

    override fun getPasscodeGenerationOptions(): PasscodeGenerationOptions? {
        val userId = authDiskSource.userState?.activeUserId
        return userId?.let { generatorDiskSource.getPasscodeGenerationOptions(it) }
    }

    override fun savePasscodeGenerationOptions(options: PasscodeGenerationOptions) {
        val userId = authDiskSource.userState?.activeUserId
        userId?.let { generatorDiskSource.storePasscodeGenerationOptions(it, options) }
    }

    override suspend fun storePasswordHistory(passwordHistoryView: PasswordHistoryView) {
        val userId = authDiskSource.userState?.activeUserId ?: return
        val encryptedPasswordHistory = vaultSdkSource
            .encryptPasswordHistory(passwordHistoryView)
            .getOrNull() ?: return
        passwordHistoryDiskSource.insertPasswordHistory(
            encryptedPasswordHistory.toPasswordHistoryEntity(userId),
        )
    }

    override suspend fun clearPasswordHistory() {
        val userId = authDiskSource.userState?.activeUserId ?: return
        passwordHistoryDiskSource.clearPasswordHistories(userId)
    }
}