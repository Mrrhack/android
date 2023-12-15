package com.x8bit.bitwarden.ui.auth.feature.login

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.x8bit.bitwarden.ui.platform.theme.TransitionProviders

private const val EMAIL_ADDRESS: String = "email_address"
private const val CAPTCHA_TOKEN = "captcha_token"
private const val LOGIN_ROUTE: String = "login/{$EMAIL_ADDRESS}?$CAPTCHA_TOKEN={$CAPTCHA_TOKEN}"

/**
 * Class to retrieve login arguments from the [SavedStateHandle].
 */
class LoginArgs(val emailAddress: String, val captchaToken: String?) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        checkNotNull(savedStateHandle[EMAIL_ADDRESS]) as String,
        savedStateHandle[CAPTCHA_TOKEN],
    )
}

/**
 * Navigate to the login screen with the given email address and region label.
 */
fun NavController.navigateToLogin(
    emailAddress: String,
    captchaToken: String?,
    navOptions: NavOptions? = null,
) {
    this.navigate(
        "login/$emailAddress?$CAPTCHA_TOKEN=$captchaToken",
        navOptions,
    )
}

/**
 * Add the Login screen to the nav graph.
 */
fun NavGraphBuilder.loginDestinations(
    onNavigateBack: () -> Unit,
) {
    composable(
        route = LOGIN_ROUTE,
        arguments = listOf(
            navArgument(EMAIL_ADDRESS) { type = NavType.StringType },
            navArgument(CAPTCHA_TOKEN) {
                type = NavType.StringType
                nullable = true
            },
        ),
        enterTransition = TransitionProviders.Enter.slideUp,
        exitTransition = TransitionProviders.Exit.slideDown,
        popEnterTransition = TransitionProviders.Enter.slideUp,
        popExitTransition = TransitionProviders.Exit.slideDown,
    ) {
        LoginScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}