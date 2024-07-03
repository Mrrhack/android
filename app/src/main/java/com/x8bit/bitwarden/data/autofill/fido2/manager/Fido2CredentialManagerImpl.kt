package com.x8bit.bitwarden.data.autofill.fido2.manager

import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.provider.CallingAppInfo
import com.bitwarden.fido.ClientData
import com.bitwarden.sdk.CheckUserResult
import com.bitwarden.sdk.CipherViewWrapper
import com.bitwarden.sdk.Fido2CredentialStore
import com.bitwarden.vault.CipherView
import com.x8bit.bitwarden.data.autofill.fido2.datasource.network.model.DigitalAssetLinkResponseJson
import com.x8bit.bitwarden.data.autofill.fido2.datasource.network.model.PublicKeyCredentialCreationOptions
import com.x8bit.bitwarden.data.autofill.fido2.datasource.network.service.DigitalAssetLinkService
import com.x8bit.bitwarden.data.autofill.fido2.model.Fido2CreateCredentialResult
import com.x8bit.bitwarden.data.autofill.fido2.model.Fido2CredentialRequest
import com.x8bit.bitwarden.data.autofill.fido2.model.Fido2ValidateOriginResult
import com.x8bit.bitwarden.data.platform.manager.AssetManager
import com.x8bit.bitwarden.data.platform.util.asFailure
import com.x8bit.bitwarden.data.platform.util.asSuccess
import com.x8bit.bitwarden.data.platform.util.flatMap
import com.x8bit.bitwarden.data.platform.util.getAppOrigin
import com.x8bit.bitwarden.data.platform.util.getAppSigningSignatureFingerprint
import com.x8bit.bitwarden.data.platform.util.getSignatureFingerprintAsHexString
import com.x8bit.bitwarden.data.platform.util.validatePrivilegedApp
import com.x8bit.bitwarden.data.vault.datasource.sdk.VaultSdkSource
import com.x8bit.bitwarden.data.vault.datasource.sdk.model.RegisterFido2CredentialRequest
import com.x8bit.bitwarden.data.vault.datasource.sdk.util.toAndroidAttestationResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val ALLOW_LIST_FILE_NAME = "fido2_privileged_allow_list.json"

/**
 * Primary implementation of [Fido2CredentialManager].
 */
class Fido2CredentialManagerImpl(
    private val assetManager: AssetManager,
    private val digitalAssetLinkService: DigitalAssetLinkService,
    private val vaultSdkSource: VaultSdkSource,
    private val fido2CredentialStore: Fido2CredentialStore,
    private val json: Json,
) : Fido2CredentialManager,
    Fido2CredentialStore by fido2CredentialStore {

    override suspend fun registerFido2Credential(
        userId: String,
        fido2CredentialRequest: Fido2CredentialRequest,
        selectedCipherView: CipherView,
    ): Fido2CreateCredentialResult {
        val clientData = if (fido2CredentialRequest.callingAppInfo.isOriginPopulated()) {
            fido2CredentialRequest.callingAppInfo.getAppSigningSignatureFingerprint()
                ?.let { ClientData.DefaultWithCustomHash(hash = it) }
                ?: return Fido2CreateCredentialResult.Error(
                    exception = CreateCredentialUnknownException(
                        errorMessage = "Application contains multiple signing certificates.",
                    ),
                )
        } else {
            ClientData.DefaultWithExtraData(
                androidPackageName = fido2CredentialRequest
                    .callingAppInfo
                    .getAppOrigin(),
            )
        }
        val origin = fido2CredentialRequest.origin
            ?: fido2CredentialRequest.callingAppInfo.getAppOrigin()

        return vaultSdkSource.registerFido2Credential(
            request = RegisterFido2CredentialRequest(
                userId = userId,
                origin = origin,
                requestJson = """{"publicKey": ${fido2CredentialRequest.requestJson}}""",
                clientData = clientData,
                selectedCipherView = selectedCipherView,
                isUserVerificationSupported = true,
            ),
            fido2CredentialStore = this,
            // TODO: [PM-8137] Determine if user verification is supported
            checkUser = { _, _ -> CheckUserResult(true, true) },
            checkUserAndPickCredential = { _, _ -> CipherViewWrapper(selectedCipherView) },
        )
            .map { it.toAndroidAttestationResponse() }
            .mapCatching { json.encodeToString(it) }
            .fold(
                onSuccess = { Fido2CreateCredentialResult.Success(it) },
                onFailure = {
                    Fido2CreateCredentialResult.Error(CreateCredentialUnknownException())
                },
            )
    }

    override suspend fun validateOrigin(
        fido2CredentialRequest: Fido2CredentialRequest,
    ): Fido2ValidateOriginResult {
        val callingAppInfo = fido2CredentialRequest.callingAppInfo
        return if (callingAppInfo.isOriginPopulated()) {
            validatePrivilegedAppOrigin(callingAppInfo)
        } else {
            validateCallingApplicationAssetLinks(fido2CredentialRequest)
        }
    }

    override fun getPasskeyCreateOptionsOrNull(
        requestJson: String,
    ): PublicKeyCredentialCreationOptions? =
        try {
            json.decodeFromString<PublicKeyCredentialCreationOptions>(requestJson)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    @Suppress("ReturnCount")
    private suspend fun validateCallingApplicationAssetLinks(
        fido2CredentialRequest: Fido2CredentialRequest,
    ): Fido2ValidateOriginResult {
        val callingAppInfo = fido2CredentialRequest.callingAppInfo
        return fido2CredentialRequest
            .requestJson
            .getRpId(json)
            .flatMap { rpId ->
                digitalAssetLinkService.getDigitalAssetLinkForRp(relyingParty = rpId)
            }
            .onFailure {
                return Fido2ValidateOriginResult.Error.AssetLinkNotFound
            }
            .map { statements ->
                statements
                    .filterMatchingAppStatementsOrNull(
                        rpPackageName = callingAppInfo.packageName,
                    )
                    ?: return Fido2ValidateOriginResult.Error.ApplicationNotFound
            }
            .map { matchingStatements ->
                callingAppInfo.getSignatureFingerprintAsHexString()
                    ?.let { certificateFingerprint ->
                        matchingStatements
                            .filterMatchingAppSignaturesOrNull(
                                signature = certificateFingerprint,
                            )
                    }
                    ?: return Fido2ValidateOriginResult.Error.ApplicationNotVerified
            }
            .fold(
                onSuccess = {
                    Fido2ValidateOriginResult.Success
                },
                onFailure = {
                    Fido2ValidateOriginResult.Error.Unknown
                },
            )
    }

    private suspend fun validatePrivilegedAppOrigin(
        callingAppInfo: CallingAppInfo,
    ): Fido2ValidateOriginResult =
        assetManager
            .readAsset(ALLOW_LIST_FILE_NAME)
            .map { allowList ->
                callingAppInfo.validatePrivilegedApp(
                    allowList = allowList,
                )
            }
            .fold(
                onSuccess = { it },
                onFailure = { Fido2ValidateOriginResult.Error.Unknown },
            )

    /**
     * Returns statements targeting the calling Android application, or null.
     */
    private fun List<DigitalAssetLinkResponseJson>.filterMatchingAppStatementsOrNull(
        rpPackageName: String,
    ): List<DigitalAssetLinkResponseJson>? =
        filter { statement ->
            val target = statement.target
            target.namespace == "android_app" &&
                target.packageName == rpPackageName &&
                statement.relation.containsAll(
                    listOf(
                        "delegate_permission/common.get_login_creds",
                        "delegate_permission/common.handle_all_urls",
                    ),
                )
        }
            .takeUnless { it.isEmpty() }

    /**
     * Returns statements that match the given [signature], or null.
     */
    private fun List<DigitalAssetLinkResponseJson>.filterMatchingAppSignaturesOrNull(
        signature: String,
    ): List<DigitalAssetLinkResponseJson>? =
        filter { statement ->
            statement.target.sha256CertFingerprints
                ?.contains(signature)
                ?: false
        }
            .takeUnless { it.isEmpty() }

    private fun String.getRpId(json: Json): Result<String> {
        return try {
            json
                .decodeFromString<PublicKeyCredentialCreationOptions>(this)
                .relyingParty
                .id
                .asSuccess()
        } catch (e: SerializationException) {
            e.asFailure()
        } catch (e: IllegalArgumentException) {
            e.asFailure()
        }
    }
}
