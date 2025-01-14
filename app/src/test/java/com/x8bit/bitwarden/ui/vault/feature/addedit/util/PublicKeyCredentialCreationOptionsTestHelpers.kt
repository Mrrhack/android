package com.x8bit.bitwarden.ui.vault.feature.addedit.util

import com.x8bit.bitwarden.data.autofill.fido2.model.PasskeyAttestationOptions

/**
 * Returns a mock FIDO 2 [PasskeyAttestationOptions] object to simulate a credential
 * creation request.
 */
@Suppress("MaxLineLength")
fun createMockPasskeyAttestationOptions(
    number: Int,
    userVerificationRequirement: PasskeyAttestationOptions.AuthenticatorSelectionCriteria.UserVerificationRequirement? = null,
) = PasskeyAttestationOptions(
    authenticatorSelection = PasskeyAttestationOptions
        .AuthenticatorSelectionCriteria(userVerification = userVerificationRequirement),
    challenge = "mockPublicKeyCredentialCreationOptionsChallenge-$number",
    excludeCredentials = listOf(
        PasskeyAttestationOptions.PublicKeyCredentialDescriptor(
            type = "mockPublicKeyCredentialDescriptorType-$number",
            id = "mockPublicKeyCredentialDescriptorId-$number",
            transports = listOf("mockPublicKeyCredentialDescriptorTransports-$number"),
        ),
    ),
    pubKeyCredParams = listOf(
        PasskeyAttestationOptions.PublicKeyCredentialParameters(
            type = "PublicKeyCredentialParametersType-$number",
            alg = number.toLong(),
        ),
    ),
    relyingParty = PasskeyAttestationOptions.PublicKeyCredentialRpEntity(
        name = "mockPublicKeyCredentialRpEntityName-$number",
        id = "mockPublicKeyCredentialRpEntity-$number",
    ),
    user = PasskeyAttestationOptions.PublicKeyCredentialUserEntity(
        name = "mockPublicKeyCredentialUserEntityName-$number",
        id = "mockPublicKeyCredentialUserEntityId-$number",
        displayName = "mockPublicKeyCredentialUserEntityDisplayName-$number",
    ),
)
