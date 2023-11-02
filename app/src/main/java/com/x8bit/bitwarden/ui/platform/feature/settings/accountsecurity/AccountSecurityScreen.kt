package com.x8bit.bitwarden.ui.platform.feature.settings.accountsecurity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.platform.base.util.EventsEffect
import com.x8bit.bitwarden.ui.platform.base.util.IntentHandler
import com.x8bit.bitwarden.ui.platform.base.util.asText
import com.x8bit.bitwarden.ui.platform.components.BitwardenExternalLinkRow
import com.x8bit.bitwarden.ui.platform.components.BitwardenListHeaderText
import com.x8bit.bitwarden.ui.platform.components.BitwardenSelectionDialog
import com.x8bit.bitwarden.ui.platform.components.BitwardenSelectionRow
import com.x8bit.bitwarden.ui.platform.components.BitwardenTextRow
import com.x8bit.bitwarden.ui.platform.components.BitwardenTopAppBar
import com.x8bit.bitwarden.ui.platform.components.BitwardenTwoButtonDialog
import com.x8bit.bitwarden.ui.platform.components.BitwardenWideSwitch

/**
 * Displays the account security screen.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountSecurityViewModel = hiltViewModel(),
    intentHandler: IntentHandler = IntentHandler(context = LocalContext.current),
) {
    val state by viewModel.stateFlow.collectAsState()
    val context = LocalContext.current
    val resources = context.resources
    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            AccountSecurityEvent.NavigateBack -> onNavigateBack()

            is AccountSecurityEvent.ShowToast -> {
                Toast.makeText(context, event.text(resources), Toast.LENGTH_SHORT).show()
            }
        }
    }

    when (state.dialog) {
        AccountSecurityDialog.ConfirmLogout -> ConfirmLogoutDialog(
            onDismiss = remember(viewModel) {
                { viewModel.trySendAction(AccountSecurityAction.DismissDialog) }
            },
            onConfirmClick = remember(viewModel) {
                { viewModel.trySendAction(AccountSecurityAction.ConfirmLogoutClick) }
            },
        )

        AccountSecurityDialog.SessionTimeoutAction -> SessionTimeoutActionDialog(
            selectedSessionTimeoutAction = state.sessionTimeoutAction,
            onDismissRequest = remember(viewModel) {
                { viewModel.trySendAction(AccountSecurityAction.DismissDialog) }
            },
            onActionSelect = remember(viewModel) {
                { viewModel.trySendAction(AccountSecurityAction.SessionTimeoutActionSelect(it)) }
            },
        )

        null -> Unit
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BitwardenTopAppBar(
                title = stringResource(id = R.string.account_security),
                scrollBehavior = scrollBehavior,
                navigationIcon = painterResource(id = R.drawable.ic_back),
                navigationIconContentDescription = stringResource(id = R.string.back),
                onNavigationIconClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.BackClick) }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState()),
        ) {
            BitwardenListHeaderText(
                label = stringResource(id = R.string.approve_login_requests),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenWideSwitch(
                label = stringResource(
                    id = R.string.use_this_device_to_approve_login_requests_made_from_other_devices,
                ),
                isChecked = state.isApproveLoginRequestsEnabled,
                onCheckedChange = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.LoginRequestToggle(it)) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenTextRow(
                text = R.string.pending_log_in_requests.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.PendingLoginRequestsClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            BitwardenListHeaderText(
                label = stringResource(id = R.string.unlock_options),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenWideSwitch(
                label = stringResource(
                    id = R.string.unlock_with,
                    stringResource(id = R.string.biometrics),
                ),
                isChecked = state.isUnlockWithBiometricsEnabled,
                onCheckedChange = remember(viewModel) {
                    {
                        viewModel.trySendAction(
                            AccountSecurityAction.UnlockWithBiometricToggle(it),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenWideSwitch(
                label = stringResource(id = R.string.unlock_with_pin),
                isChecked = state.isUnlockWithPinEnabled,
                onCheckedChange = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.UnlockWithPinToggle(it)) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(16.dp))
            BitwardenListHeaderText(
                label = stringResource(id = R.string.session_timeout),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenTextRow(
                text = R.string.session_timeout.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.SessionTimeoutClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = state.sessionTimeout(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BitwardenTextRow(
                text = R.string.session_timeout_action.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.SessionTimeoutActionClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = state.sessionTimeoutAction.text(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))
            BitwardenListHeaderText(
                label = stringResource(id = R.string.other),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            BitwardenTextRow(
                text = R.string.account_fingerprint_phrase.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.AccountFingerprintPhraseClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            BitwardenExternalLinkRow(
                text = R.string.two_step_login.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.TwoStepLoginClick) }
                },
                withDivider = false,
                modifier = Modifier.fillMaxWidth(),
            )
            BitwardenExternalLinkRow(
                text = R.string.change_master_password.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.ChangeMasterPasswordClick) }
                },
                withDivider = false,
                modifier = Modifier.fillMaxWidth(),
            )
            BitwardenTextRow(
                text = R.string.lock_now.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.LockNowClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            BitwardenTextRow(
                text = R.string.log_out.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.LogoutClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            BitwardenTextRow(
                text = R.string.delete_account.asText(),
                onClick = remember(viewModel) {
                    { viewModel.trySendAction(AccountSecurityAction.DeleteAccountClick) }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConfirmLogoutDialog(
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    BitwardenTwoButtonDialog(
        title = R.string.log_out.asText(),
        message = R.string.logout_confirmation.asText(),
        confirmButtonText = R.string.yes.asText(),
        onConfirmClick = onConfirmClick,
        dismissButtonText = R.string.cancel.asText(),
        onDismissClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun SessionTimeoutActionDialog(
    selectedSessionTimeoutAction: SessionTimeoutAction,
    onDismissRequest: () -> Unit,
    onActionSelect: (SessionTimeoutAction) -> Unit,
) {
    BitwardenSelectionDialog(
        title = R.string.vault_timeout_action.asText(),
        onDismissRequest = onDismissRequest,
    ) {
        SessionTimeoutAction.values().forEach { option ->
            BitwardenSelectionRow(
                text = option.text,
                isSelected = option == selectedSessionTimeoutAction,
                onClick = { onActionSelect(SessionTimeoutAction.values().first { it == option }) },
            )
        }
    }
}