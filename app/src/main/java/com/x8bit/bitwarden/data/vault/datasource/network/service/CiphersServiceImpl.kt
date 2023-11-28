package com.x8bit.bitwarden.data.vault.datasource.network.service

import CipherJsonRequest
import com.x8bit.bitwarden.data.vault.datasource.network.api.CiphersApi
import com.x8bit.bitwarden.data.vault.datasource.network.model.SyncResponseJson

class CiphersServiceImpl constructor(
    private val ciphersApi: CiphersApi,
) : CiphersService {
    override suspend fun createCipher(body: CipherJsonRequest): Result<SyncResponseJson.Cipher> =
        ciphersApi.createCipher(body = body)
}