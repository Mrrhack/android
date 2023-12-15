package com.x8bit.bitwarden.data.auth.repository.di

import com.x8bit.bitwarden.data.auth.datasource.disk.AuthDiskSource
import com.x8bit.bitwarden.data.auth.datasource.network.service.AccountsService
import com.x8bit.bitwarden.data.auth.datasource.network.service.HaveIBeenPwnedService
import com.x8bit.bitwarden.data.auth.datasource.network.service.IdentityService
import com.x8bit.bitwarden.data.auth.datasource.sdk.AuthSdkSource
import com.x8bit.bitwarden.data.auth.repository.AuthRepository
import com.x8bit.bitwarden.data.auth.repository.AuthRepositoryImpl
import com.x8bit.bitwarden.data.platform.manager.dispatcher.DispatcherManager
import com.x8bit.bitwarden.data.platform.repository.EnvironmentRepository
import com.x8bit.bitwarden.data.vault.repository.VaultRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides repositories in the auth package.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {

    @Provides
    @Singleton
    fun providesAuthRepository(
        accountsService: AccountsService,
        identityService: IdentityService,
        haveIBeenPwnedService: HaveIBeenPwnedService,
        authSdkSource: AuthSdkSource,
        authDiskSource: AuthDiskSource,
        dispatchers: DispatcherManager,
        environmentRepository: EnvironmentRepository,
        vaultRepository: VaultRepository,
    ): AuthRepository = AuthRepositoryImpl(
        accountsService = accountsService,
        identityService = identityService,
        authSdkSource = authSdkSource,
        authDiskSource = authDiskSource,
        haveIBeenPwnedService = haveIBeenPwnedService,
        dispatcherManager = dispatchers,
        environmentRepository = environmentRepository,
        vaultRepository = vaultRepository,
    )
}