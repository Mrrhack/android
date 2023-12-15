package com.x8bit.bitwarden.ui.auth.feature.createaccount

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.net.toUri
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.AcceptPoliciesToggle
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.CheckDataBreachesToggle
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.CloseClick
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.ConfirmPasswordInputChange
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.EmailInputChange
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.PasswordHintChange
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.PasswordInputChange
import com.x8bit.bitwarden.ui.auth.feature.createaccount.CreateAccountAction.SubmitClick
import com.x8bit.bitwarden.ui.platform.base.BaseComposeTest
import com.x8bit.bitwarden.ui.platform.base.util.IntentHandler
import com.x8bit.bitwarden.ui.platform.base.util.asText
import com.x8bit.bitwarden.ui.platform.components.BasicDialogState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAccountScreenTest : BaseComposeTest() {

    @Test
    fun `app bar submit click should send SubmitClick action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(SubmitClick) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNodeWithText("Submit").performClick()
        verify { viewModel.trySendAction(SubmitClick) }
    }

    @Test
    fun `close click should send CloseClick action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CloseClick) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        verify { viewModel.trySendAction(CloseClick) }
    }

    @Test
    fun `check data breaches click should send CheckDataBreachesToggle action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CheckDataBreachesToggle(true)) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onNodeWithText("Check known data breaches for this password")
            .performScrollTo()
            .performClick()
        verify { viewModel.trySendAction(CheckDataBreachesToggle(true)) }
    }

    @Test
    fun `accept policies should be toggled on or off according to the state`() {
        val mutableStateFlow = MutableStateFlow(DEFAULT_STATE)
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns mutableStateFlow
            every { eventFlow } returns emptyFlow()
            every { trySendAction(AcceptPoliciesToggle(true)) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onNodeWithText("By activating this switch you agree", substring = true)
            .assertIsOff()

        mutableStateFlow.update { it.copy(isAcceptPoliciesToggled = true) }

        composeTestRule
            .onNodeWithText("By activating this switch you agree", substring = true)
            .assertIsOn()
    }

    @Test
    fun `accept policies click should send AcceptPoliciesToggle action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(AcceptPoliciesToggle(true)) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onNodeWithText("By activating this switch you agree", substring = true)
            .performScrollTo()
            .performClick()
        verify { viewModel.trySendAction(AcceptPoliciesToggle(true)) }
    }

    @Test
    fun `NavigateBack event should invoke navigate back lambda`() {
        var onNavigateBackCalled = false
        val onNavigateBack = { onNavigateBackCalled = true }
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns flowOf(CreateAccountEvent.NavigateBack)
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        assertTrue(onNavigateBackCalled)
    }

    @Test
    fun `NavigateToLogin event should invoke navigate login lambda`() {
        var onNavigateToLoginCalled = false
        val onNavigateToLogin = { _: String, _: String -> onNavigateToLoginCalled = true }
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns flowOf(
                CreateAccountEvent.NavigateToLogin(
                    email = "",
                    captchaToken = "",
                ),
            )
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = onNavigateToLogin,
                viewModel = viewModel,
            )
        }
        assertTrue(onNavigateToLoginCalled)
    }

    @Test
    fun `NavigateToCaptcha event should invoke intent handler`() {
        val mockUri = mockk<Uri>()
        val intentHandler = mockk<IntentHandler>(relaxed = true) {
            every { startCustomTabsActivity(mockUri) } returns Unit
        }
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns flowOf(
                CreateAccountEvent.NavigateToCaptcha(
                    uri = mockUri,
                ),
            )
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
                intentHandler = intentHandler,
            )
        }
        verify {
            intentHandler.startCustomTabsActivity(mockUri)
        }
    }

    @Test
    fun `NavigateToPrivacyPolicy event should invoke intent handler`() {
        val expectedIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://bitwarden.com/privacy/"))
        val intentHandler = mockk<IntentHandler>(relaxed = true) {
            every { startActivity(expectedIntent) } returns Unit
        }
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns flowOf(CreateAccountEvent.NavigateToPrivacyPolicy)
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
                intentHandler = intentHandler,
            )
        }
        verify {
            intentHandler.launchUri("https://bitwarden.com/privacy/".toUri())
        }
    }

    @Test
    fun `NavigateToTerms event should invoke intent handler`() {
        val expectedIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://bitwarden.com/terms/"))
        val intentHandler = mockk<IntentHandler>(relaxed = true) {
            every { startActivity(expectedIntent) } returns Unit
        }
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns flowOf(CreateAccountEvent.NavigateToTerms)
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
                intentHandler = intentHandler,
            )
        }
        verify {
            intentHandler.launchUri("https://bitwarden.com/terms/".toUri())
        }
    }

    @Test
    fun `email input change should send EmailInputChange action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(EmailInputChange("input")) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNodeWithText("Email address").performTextInput(TEST_INPUT)
        verify { viewModel.trySendAction(EmailInputChange(TEST_INPUT)) }
    }

    @Test
    fun `password input change should send PasswordInputChange action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(PasswordInputChange("input")) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNodeWithText("Master password").performTextInput(TEST_INPUT)
        verify { viewModel.trySendAction(PasswordInputChange(TEST_INPUT)) }
    }

    @Test
    fun `confirm password input change should send ConfirmPasswordInputChange action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(ConfirmPasswordInputChange("input")) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNodeWithText("Re-type master password").performTextInput(TEST_INPUT)
        verify { viewModel.trySendAction(ConfirmPasswordInputChange(TEST_INPUT)) }
    }

    @Test
    fun `password hint input change should send PasswordHintChange action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
            every { trySendAction(PasswordHintChange("input")) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onNodeWithText("Master password hint (optional)")
            .performTextInput(TEST_INPUT)
        verify { viewModel.trySendAction(PasswordHintChange(TEST_INPUT)) }
    }

    @Test
    fun `clicking OK on the error dialog should send ErrorDialogDismiss action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(
                DEFAULT_STATE.copy(
                    dialog = CreateAccountDialog.Error(
                        BasicDialogState.Shown(
                            title = "title".asText(),
                            message = "message".asText(),
                        ),
                    ),
                ),
            )
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CreateAccountAction.ErrorDialogDismiss) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onAllNodesWithText("Ok")
            .filterToOne(hasAnyAncestor(isDialog()))
            .performClick()
        verify { viewModel.trySendAction(CreateAccountAction.ErrorDialogDismiss) }
    }

    @Test
    fun `clicking No on the HIBP dialog should send ErrorDialogDismiss action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(
                DEFAULT_STATE.copy(dialog = CreateAccountDialog.HaveIBeenPwned),
            )
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CreateAccountAction.ErrorDialogDismiss) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onAllNodesWithText("No")
            .filterToOne(hasAnyAncestor(isDialog()))
            .performClick()
        verify { viewModel.trySendAction(CreateAccountAction.ErrorDialogDismiss) }
    }

    @Test
    fun `clicking Yes on the HIBP dialog should send ContinueWithBreachedPasswordClick action`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(
                DEFAULT_STATE.copy(dialog = CreateAccountDialog.HaveIBeenPwned),
            )
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CreateAccountAction.ErrorDialogDismiss) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule
            .onAllNodesWithText("Yes")
            .filterToOne(hasAnyAncestor(isDialog()))
            .performClick()
        verify { viewModel.trySendAction(CreateAccountAction.ContinueWithBreachedPasswordClick) }
    }

    @Test
    fun `when BasicDialogState is Shown should show dialog`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(
                DEFAULT_STATE.copy(
                    dialog = CreateAccountDialog.Error(
                        BasicDialogState.Shown(
                            title = "title".asText(),
                            message = "message".asText(),
                        ),
                    ),
                ),
            )
            every { eventFlow } returns emptyFlow()
            every { trySendAction(CreateAccountAction.ErrorDialogDismiss) } returns Unit
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
    }

    @Test
    fun `password strength should change as state changes`() {
        val mutableStateFlow = MutableStateFlow(DEFAULT_STATE)
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns mutableStateFlow
            every { eventFlow } returns emptyFlow()
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }
        mutableStateFlow.update {
            DEFAULT_STATE.copy(passwordStrengthState = PasswordStrengthState.WEAK_1)
        }
        composeTestRule.onNodeWithText("Weak").assertIsDisplayed()

        mutableStateFlow.update {
            DEFAULT_STATE.copy(passwordStrengthState = PasswordStrengthState.WEAK_2)
        }
        composeTestRule.onNodeWithText("Weak").assertIsDisplayed()

        mutableStateFlow.update {
            DEFAULT_STATE.copy(passwordStrengthState = PasswordStrengthState.WEAK_3)
        }
        composeTestRule.onNodeWithText("Weak").assertIsDisplayed()

        mutableStateFlow.update {
            DEFAULT_STATE.copy(passwordStrengthState = PasswordStrengthState.GOOD)
        }
        composeTestRule.onNodeWithText("Good").assertIsDisplayed()

        mutableStateFlow.update {
            DEFAULT_STATE.copy(passwordStrengthState = PasswordStrengthState.STRONG)
        }
        composeTestRule.onNodeWithText("Strong").assertIsDisplayed()
    }

    @Test
    fun `toggling one password field visibility should toggle the other`() {
        val viewModel = mockk<CreateAccountViewModel>(relaxed = true) {
            every { stateFlow } returns MutableStateFlow(DEFAULT_STATE)
            every { eventFlow } returns emptyFlow()
        }
        composeTestRule.setContent {
            CreateAccountScreen(
                onNavigateBack = {},
                onNavigateToLogin = { _, _ -> },
                viewModel = viewModel,
            )
        }

        // should start with 2 Show buttons:
        composeTestRule
            .onAllNodesWithContentDescription("Show")
            .assertCountEquals(2)
            .get(0)
            .performClick()

        // after clicking there should be no Show buttons:
        composeTestRule
            .onAllNodesWithContentDescription("Show")
            .assertCountEquals(0)

        // and there should be 2 hide buttons now, and we'll click the second one:
        composeTestRule
            .onAllNodesWithContentDescription("Hide")
            .assertCountEquals(2)
            .get(1)
            .performClick()

        // then there should be two show buttons again
        composeTestRule
            .onAllNodesWithContentDescription("Show")
            .assertCountEquals(2)
    }

    companion object {
        private const val TEST_INPUT = "input"
        private val DEFAULT_STATE = CreateAccountState(
            emailInput = "",
            passwordInput = "",
            confirmPasswordInput = "",
            passwordHintInput = "",
            isCheckDataBreachesToggled = false,
            isAcceptPoliciesToggled = false,
            dialog = null,
            passwordStrengthState = PasswordStrengthState.NONE,
        )
    }
}