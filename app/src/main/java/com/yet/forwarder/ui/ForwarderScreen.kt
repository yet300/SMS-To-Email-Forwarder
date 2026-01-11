package com.yet.forwarder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yet.forwarder.R
import com.yet.forwarder.ui.component.botton.ActionButton
import com.yet.forwarder.ui.component.card.SectionCard
import com.yet.forwarder.ui.component.switch.SettingSwitchItem
import com.yet.forwarder.ui.component.textfield.ConfigTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwarderScreen(
    state: ForwarderUiState,
    snackbarHostState: SnackbarHostState,
    hasSmsPermission: Boolean,
    onSmtpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onReceiverEmailChange: (String) -> Unit,
    onUseStartTlsChange: (Boolean) -> Unit,
    onRequireAuthChange: (Boolean) -> Unit,
    onUseSslChange: (Boolean) -> Unit,
    onTrustAllCertificatesChange: (Boolean) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSave: () -> Unit,
    onSendTest: () -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onMessageConsumed: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val copyError = stringResource(id = R.string.btn_copy_error)
    val copedError = stringResource(id = R.string.msg_error_copied)

    LaunchedEffect(state.message) {
        state.message?.let {
            val result = if (state.lastError != null) {
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = copyError
                )
            } else {
                snackbarHostState.showSnackbar(it)
            }
            if (result == SnackbarResult.ActionPerformed && state.lastError != null) {
                clipboardManager.setText(AnnotatedString(state.lastError))
                snackbarHostState.showSnackbar(copedError)
            }
            onMessageConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonitoringStatus(
                    monitoringEnabled = state.monitoringEnabled,
                    hasSmsPermission = hasSmsPermission
                )
            }

            item {
                ServerConfigSection(
                    state = state,
                    onSmtpChange = onSmtpChange,
                    onPortChange = onPortChange,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onReceiverEmailChange = onReceiverEmailChange,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                )
            }

            item {
                SecuritySection(
                    state = state,
                    onUseStartTlsChange = onUseStartTlsChange,
                    onRequireAuthChange = onRequireAuthChange,
                    onUseSslChange = onUseSslChange,
                    onTrustAllCertificatesChange = onTrustAllCertificatesChange
                )
            }

            item {
                ThirdPartyWarningSection()
            }

            item {
                ActionsSection(
                    state = state,
                    onSave = onSave,
                    onSendTest = onSendTest,
                    onStartMonitoring = onStartMonitoring,
                    onStopMonitoring = onStopMonitoring,
                    hasSmsPermission = hasSmsPermission,
                )
            }

            item {
                OpenSourceSection()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MonitoringStatus(monitoringEnabled: Boolean, hasSmsPermission: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = {},
            label = {
                val textRes = if (monitoringEnabled) {
                    R.string.monitoring_enabled
                } else {
                    R.string.monitoring_disabled
                }
                Text(stringResource(id = textRes))
            },
            leadingIcon = {
                val icon =
                    if (monitoringEnabled) Icons.Filled.CheckCircle else Icons.Filled.PauseCircle
                Icon(imageVector = icon, contentDescription = null)
            }
        )
        if (!hasSmsPermission) {
            Text(
                text = stringResource(id = R.string.no_sms_permission),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ServerConfigSection(
    state: ForwarderUiState,
    onSmtpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onReceiverEmailChange: (String) -> Unit,

    onTogglePasswordVisibility: () -> Unit
) {
    SectionCard(titleRes = R.string.section_smtp) {
        ConfigTextField(
            value = state.smtpServer,
            onValueChange = onSmtpChange,
            labelRes = R.string.label_smtp_server,
            leadingIcon = Icons.Default.Dns,
            imeAction = ImeAction.Next
        )
        ConfigTextField(
            value = state.portText,
            onValueChange = onPortChange,
            labelRes = R.string.label_port,
            leadingIcon = Icons.Default.Numbers,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
        ConfigTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            labelRes = R.string.label_username,
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next
        )
        ConfigTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            labelRes = R.string.label_password,
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePassword = onTogglePasswordVisibility,
            imeAction = ImeAction.Next
        )
        ConfigTextField(
            value = state.receiverEmail,
            onValueChange = onReceiverEmailChange,
            labelRes = R.string.label_receiver_email,
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        )
    }
}

@Composable
private fun SecuritySection(
    state: ForwarderUiState,
    onUseStartTlsChange: (Boolean) -> Unit,
    onRequireAuthChange: (Boolean) -> Unit,
    onUseSslChange: (Boolean) -> Unit,
    onTrustAllCertificatesChange: (Boolean) -> Unit
) {
    SectionCard(titleRes = R.string.section_security) {
        SettingSwitchItem(
            titleRes = R.string.title_starttls,
            subtitleRes = R.string.subtitle_starttls,
            checked = state.useStartTls,
            onCheckedChange = onUseStartTlsChange
        )
        HorizontalDivider()
        SettingSwitchItem(
            titleRes = R.string.title_auth,
            subtitleRes = R.string.subtitle_auth,
            checked = state.requireAuth,
            onCheckedChange = onRequireAuthChange
        )
        HorizontalDivider()
        SettingSwitchItem(
            titleRes = R.string.title_ssl,
            subtitleRes = R.string.subtitle_ssl,
            checked = state.useSsl,
            onCheckedChange = onUseSslChange
        )
        HorizontalDivider()
        SettingSwitchItem(
            titleRes = R.string.title_trust_all,
            subtitleRes = R.string.subtitle_trust_all,
            checked = state.trustAllCertificates,
            onCheckedChange = onTrustAllCertificatesChange
        )
    }
}

@Composable
private fun ThirdPartyWarningSection() {
    SectionCard(
        titleRes = R.string.section_third_party_warning,
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.section_third_party_warning_description),
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun OpenSourceSection() {
    val uriHandler = LocalUriHandler.current
    SectionCard(titleRes = R.string.section_open_source) {
        val description = stringResource(id = R.string.open_source_description)
        val linkText = stringResource(id = R.string.open_source_link_text)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )

        FilledTonalButton(
            onClick = {
                uriHandler.openUri("https://github.com/yet300/SMS-To-Email-Forwarder")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = linkText)
        }
    }
}

@Composable
private fun ActionsSection(
    state: ForwarderUiState,
    onSave: () -> Unit,
    onSendTest: () -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,

    hasSmsPermission: Boolean,
) {
    SectionCard(titleRes = R.string.section_actions) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                textRes = R.string.btn_save,
                isLoading = state.isSaving,
                isEnabled = !state.isSaving && !state.isSendingTest,
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                textRes = R.string.btn_send_test,
                isLoading = state.isSendingTest,
                isEnabled = !state.isSaving && !state.isSendingTest,
                onClick = onSendTest,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isMonitoring = state.monitoringEnabled
            val canInteract = !state.isSaving && !state.isSendingTest

            ElevatedButton(
                onClick = {
                    if (isMonitoring) onStopMonitoring()
                    else onStartMonitoring()
                },
                enabled = canInteract,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (isMonitoring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (isMonitoring) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(if (isMonitoring) R.string.btn_stop else R.string.btn_start))
            }
        }

        if (!hasSmsPermission) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sms_permission_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
