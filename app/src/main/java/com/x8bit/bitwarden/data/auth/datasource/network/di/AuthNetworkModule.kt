package com.x8bit.bitwarden.data.auth.datasource.network.di

import com.x8bit.bitwarden.data.auth.datasource.network.service.AccountsService
import com.x8bit.bitwarden.data.auth.datasource.network.service.AccountsServiceImpl
import com.x8bit.bitwarden.data.auth.datasource.network.service.HaveIBeenPwnedService
import com.x8bit.bitwarden.data.auth.datasource.network.service.HaveIBeenPwnedServiceImpl
import com.x8bit.bitwarden.data.auth.datasource.network.service.IdentityService
import com.x8bit.bitwarden.data.auth.datasource.network.service.IdentityServiceImpl
import com.x8bit.bitwarden.data.platform.datasource.network.retrofit.Retrofits
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import retrofit2.create
import javax.inject.Singleton

/**
 * Provides network dependencies in the auth package.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {

    @Provides
    @Singleton
    fun providesAccountService(
        retrofits: Retrofits,
        json: Json,
    ): AccountsService = AccountsServiceImpl(
        accountsApi = retrofits.unauthenticatedApiRetrofit.create(),
        authenticatedAccountsApi = retrofits.authenticatedApiRetrofit.create(),
        json = json,
    )

    @Provides
    @Singleton
    fun providesIdentityService(
        retrofits: Retrofits,
        json: Json,
    ): IdentityService = IdentityServiceImpl(
        api = retrofits.unauthenticatedIdentityRetrofit.create(),
        json = json,
    )

    @Provides
    @Singleton
    fun providesHaveIBeenPwnedService(
        retrofits: Retrofits,
    ): HaveIBeenPwnedService = HaveIBeenPwnedServiceImpl(
        retrofits
            .staticRetrofitBuilder
            .baseUrl("https://api.pwnedpasswords.com")
            .build()
            .create(),
    )
}