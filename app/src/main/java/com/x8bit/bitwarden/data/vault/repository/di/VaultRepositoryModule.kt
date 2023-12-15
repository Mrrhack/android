package com.x8bit.bitwarden.data.vault.repository.di

import com.x8bit.bitwarden.data.auth.datasource.disk.AuthDiskSource
import com.x8bit.bitwarden.data.platform.manager.dispatcher.DispatcherManager
import com.x8bit.bitwarden.data.vault.datasource.network.service.CiphersService
import com.x8bit.bitwarden.data.vault.datasource.network.service.SyncService
import com.x8bit.bitwarden.data.vault.datasource.sdk.VaultSdkSource
import com.x8bit.bitwarden.data.vault.repository.VaultRepository
import com.x8bit.bitwarden.data.vault.repository.VaultRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides repositories in the vault package.
 */
@Module
@InstallIn(SingletonComponent::class)
object VaultRepositoryModule {

    @Provides
    @Singleton
    fun providesVaultRepository(
        syncService: SyncService,
        ciphersService: CiphersService,
        vaultSdkSource: VaultSdkSource,
        authDiskSource: AuthDiskSource,
        dispatcherManager: DispatcherManager,
    ): VaultRepository = VaultRepositoryImpl(
        syncService = syncService,
        ciphersService = ciphersService,
        vaultSdkSource = vaultSdkSource,
        authDiskSource = authDiskSource,
        dispatcherManager = dispatcherManager,
    )
}