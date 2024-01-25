package com.x8bit.bitwarden.data.autofill.util

import android.app.assist.AssistStructure
import android.view.View
import com.x8bit.bitwarden.data.autofill.model.AutofillView

/**
 * The class name of the android edit text field.
 */
private const val ANDROID_EDIT_TEXT_CLASS_NAME: String = "android.widget.EditText"

/**
 * The set of raw autofill hints that should be ignored.
 */
private val IGNORED_RAW_HINTS: List<String> = listOf(
    "search",
    "find",
    "recipient",
    "edit",
)

/**
 * The supported password autofill hints.
 */
private val SUPPORTED_RAW_PASSWORD_HINTS: List<String> = listOf(
    "password",
    "pswd",
)

/**
 * The supported raw autofill hints.
 */
private val SUPPORTED_RAW_USERNAME_HINTS: List<String> = listOf(
    "email",
    "phone",
    "username",
)

/**
 * The supported autofill Android View hints.
 */
private val SUPPORTED_VIEW_HINTS: List<String> = listOf(
    View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
    View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
    View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
    View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,
    View.AUTOFILL_HINT_EMAIL_ADDRESS,
    View.AUTOFILL_HINT_PASSWORD,
    View.AUTOFILL_HINT_USERNAME,
)

/**
 * Whether this [AssistStructure.ViewNode] represents an input field.
 */
private val AssistStructure.ViewNode.isInputField: Boolean
    get() = className == ANDROID_EDIT_TEXT_CLASS_NAME || htmlInfo.isInputField

/**
 * Attempt to convert this [AssistStructure.ViewNode] into an [AutofillView]. If the view node
 * doesn't contain a valid autofillId, it isn't an a view setup for autofill, so we return null. If
 * it doesn't have a supported hint and isn't an input field, we also return null.
 */
fun AssistStructure.ViewNode.toAutofillView(): AutofillView? =
    this
        .autofillId
        // We only care about nodes with a valid `AutofillId`.
        ?.let { nonNullAutofillId ->
            val supportedHint = this
                .autofillHints
                ?.firstOrNull { SUPPORTED_VIEW_HINTS.contains(it) }

            if (supportedHint != null || this.isInputField) {
                val autofillViewData = AutofillView.Data(
                    autofillId = nonNullAutofillId,
                    idPackage = idPackage,
                    isFocused = isFocused,
                    webDomain = webDomain,
                    webScheme = webScheme,
                )
                buildAutofillView(
                    autofillViewData = autofillViewData,
                    supportedHint = supportedHint,
                )
            } else {
                null
            }
        }

/**
 * Attempt to convert this [AssistStructure.ViewNode] and [autofillViewData] into an [AutofillView].
 */
private fun AssistStructure.ViewNode.buildAutofillView(
    autofillViewData: AutofillView.Data,
    supportedHint: String?,
): AutofillView? = when {
    supportedHint == View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH -> {
        AutofillView.Card.ExpirationMonth(
            data = autofillViewData,
        )
    }

    supportedHint == View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR -> {
        AutofillView.Card.ExpirationYear(
            data = autofillViewData,
        )
    }

    supportedHint == View.AUTOFILL_HINT_CREDIT_CARD_NUMBER -> {
        AutofillView.Card.Number(
            data = autofillViewData,
        )
    }

    supportedHint == View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE -> {
        AutofillView.Card.SecurityCode(
            data = autofillViewData,
        )
    }

    this.isPasswordField(supportedHint) -> {
        AutofillView.Login.Password(
            data = autofillViewData,
        )
    }

    this.isUsernameField(supportedHint) -> {
        AutofillView.Login.Username(
            data = autofillViewData,
        )
    }

    else -> null
}

/**
 * Check whether this [AssistStructure.ViewNode] represents a password field.
 */
@Suppress("ReturnCount")
fun AssistStructure.ViewNode.isPasswordField(
    supportedHint: String?,
): Boolean {
    if (supportedHint == View.AUTOFILL_HINT_PASSWORD) return true

    if (this.hint?.containsAnyTerms(SUPPORTED_RAW_PASSWORD_HINTS) == true) return true

    val isInvalidField = this.idEntry?.containsAnyTerms(IGNORED_RAW_HINTS) == true ||
        this.hint?.containsAnyTerms(IGNORED_RAW_HINTS) == true
    val isUsernameField = this.isUsernameField(supportedHint)
    if (this.inputType.isPasswordInputType && !isInvalidField && !isUsernameField) return true

    return this
        .htmlInfo
        .isPasswordField()
}

/**
 * Check whether this [AssistStructure.ViewNode] represents a username field.
 */
fun AssistStructure.ViewNode.isUsernameField(
    supportedHint: String?,
): Boolean =
    supportedHint == View.AUTOFILL_HINT_USERNAME ||
        supportedHint == View.AUTOFILL_HINT_EMAIL_ADDRESS ||
        inputType.isUsernameInputType ||
        idEntry?.containsAnyTerms(SUPPORTED_RAW_USERNAME_HINTS) == true ||
        hint?.containsAnyTerms(SUPPORTED_RAW_USERNAME_HINTS) == true
