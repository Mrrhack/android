package com.x8bit.bitwarden.ui.vault.feature.item

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.x8bit.bitwarden.ui.platform.theme.TransitionProviders

private const val VAULT_ITEM_PREFIX = "vault_item"
private const val VAULT_ITEM_ID = "vault_item_id"
private const val VAULT_ITEM_ROUTE = "$VAULT_ITEM_PREFIX/{$VAULT_ITEM_ID}"

/**
 * Class to retrieve vault item arguments from the [SavedStateHandle].
 */
class VaultItemArgs(val vaultItemId: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        checkNotNull(savedStateHandle[VAULT_ITEM_ID]) as String,
    )
}

/**
 * Add the vault item screen to the nav graph.
 */
fun NavGraphBuilder.vaultItemDestination(
    onNavigateBack: () -> Unit,
    onNavigateToVaultEditItem: (vaultItemId: String) -> Unit,
) {
    composable(
        route = VAULT_ITEM_ROUTE,
        arguments = listOf(
            navArgument(VAULT_ITEM_ID) { type = NavType.StringType },
        ),
        enterTransition = TransitionProviders.Enter.slideUp,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.slideDown,
    ) {
        VaultItemScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToVaultEditItem = onNavigateToVaultEditItem,
        )
    }
}

/**
 * Navigate to the vault item screen.
 */
fun NavController.navigateToVaultItem(
    vaultItemId: String,
    navOptions: NavOptions? = null,
) {
    navigate("$VAULT_ITEM_PREFIX/$vaultItemId", navOptions)
}