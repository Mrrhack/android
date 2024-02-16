package com.x8bit.bitwarden.data.auth.manager.di

import android.content.Context
import com.x8bit.bitwarden.data.auth.datasource.disk.AuthDiskSource
import com.x8bit.bitwarden.data.auth.datasource.network.service.AuthRequestsService
import com.x8bit.bitwarden.data.auth.datasource.network.service.NewAuthRequestService
import com.x8bit.bitwarden.data.auth.datasource.sdk.AuthSdkSource
import com.x8bit.bitwarden.data.auth.manager.AuthRequestManager
import com.x8bit.bitwarden.data.auth.manager.AuthRequestManagerImpl
import com.x8bit.bitwarden.data.auth.manager.AuthRequestNotificationManager
import com.x8bit.bitwarden.data.auth.manager.AuthRequestNotificationManagerImpl
import com.x8bit.bitwarden.data.auth.manager.UserLogoutManager
import com.x8bit.bitwarden.data.auth.manager.UserLogoutManagerImpl
import com.x8bit.bitwarden.data.platform.datasource.disk.PushDiskSource
import com.x8bit.bitwarden.data.platform.datasource.disk.SettingsDiskSource
import com.x8bit.bitwarden.data.platform.manager.PushManager
import com.x8bit.bitwarden.data.platform.manager.dispatcher.DispatcherManager
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.GeneratorDiskSource
import com.x8bit.bitwarden.data.tools.generator.datasource.disk.PasswordHistoryDiskSource
import com.x8bit.bitwarden.data.vault.datasource.disk.VaultDiskSource
import com.x8bit.bitwarden.data.vault.datasource.sdk.VaultSdkSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Provides managers in the auth package.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthManagerModule {

    @Provides
    @Singleton
    fun provideAuthRequestNotificationManager(
        @ApplicationContext context: Context,
        authDiskSource: AuthDiskSource,
        pushManager: PushManager,
        dispatchers: DispatcherManager,
    ): AuthRequestNotificationManager =
        AuthRequestNotificationManagerImpl(
            context = context,
            authDiskSource = authDiskSource,
            pushManager = pushManager,
            dispatchers = dispatchers,
        )

    @Provides
    @Singleton
    fun provideAuthRequestManager(
        clock: Clock,
        authRequestsService: AuthRequestsService,
        newAuthRequestService: NewAuthRequestService,
        authSdkSource: AuthSdkSource,
        vaultSdkSource: VaultSdkSource,
        authDiskSource: AuthDiskSource,
    ): AuthRequestManager =
        AuthRequestManagerImpl(
            clock = clock,
            authRequestsService = authRequestsService,
            newAuthRequestService = newAuthRequestService,
            authSdkSource = authSdkSource,
            vaultSdkSource = vaultSdkSource,
            authDiskSource = authDiskSource,
        )

    @Provides
    @Singleton
    fun provideUserLogoutManager(
        @ApplicationContext context: Context,
        authDiskSource: AuthDiskSource,
        generatorDiskSource: GeneratorDiskSource,
        passwordHistoryDiskSource: PasswordHistoryDiskSource,
        pushDiskSource: PushDiskSource,
        settingsDiskSource: SettingsDiskSource,
        vaultDiskSource: VaultDiskSource,
        dispatcherManager: DispatcherManager,
    ): UserLogoutManager =
        UserLogoutManagerImpl(
            context = context,
            authDiskSource = authDiskSource,
            generatorDiskSource = generatorDiskSource,
            passwordHistoryDiskSource = passwordHistoryDiskSource,
            pushDiskSource = pushDiskSource,
            settingsDiskSource = settingsDiskSource,
            vaultDiskSource = vaultDiskSource,
            dispatcherManager = dispatcherManager,
        )
}
