package com.yet.forwarder.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yet.forwarder.R

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

    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonitoringStatus(
                monitoringEnabled = state.monitoringEnabled,
                hasSmsPermission = hasSmsPermission
            )

            OutlinedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.section_smtp), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.smtpServer,
                        onValueChange = onSmtpChange,
                        label = { Text(stringResource(id = R.string.label_smtp_server)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.portText,
                        onValueChange = onPortChange,
                        label = { Text(stringResource(id = R.string.label_port)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = onUsernameChange,
                        label = { Text(stringResource(id = R.string.label_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text(stringResource(id = R.string.label_password)) },
                        singleLine = true,
                        visualTransformation = if (state.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            val icon = if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = onTogglePasswordVisibility) {
                                val description = if (state.passwordVisible) {
                                    stringResource(id = R.string.title_password_visibility_off)
                                } else {
                                    stringResource(id = R.string.title_password_visibility_on)
                                }
                                Icon(imageVector = icon, contentDescription = description)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.receiverEmail,
                        onValueChange = onReceiverEmailChange,
                        label = { Text(stringResource(id = R.string.label_receiver_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.section_security), style = MaterialTheme.typography.titleMedium)
                    SwitchRow(
                        title = stringResource(id = R.string.title_starttls),
                        subtitle = stringResource(id = R.string.subtitle_starttls),
                        checked = state.useStartTls,
                        onCheckedChange = onUseStartTlsChange
                    )
                    SwitchRow(
                        title = stringResource(id = R.string.title_auth),
                        subtitle = stringResource(id = R.string.subtitle_auth),
                        checked = state.requireAuth,
                        onCheckedChange = onRequireAuthChange
                    )
                    SwitchRow(
                        title = stringResource(id = R.string.title_ssl),
                        subtitle = stringResource(id = R.string.subtitle_ssl),
                        checked = state.useSsl,
                        onCheckedChange = onUseSslChange
                    )
                    SwitchRow(
                        title = stringResource(id = R.string.title_trust_all),
                        subtitle = stringResource(id = R.string.subtitle_trust_all),
                        checked = state.trustAllCertificates,
                        onCheckedChange = onTrustAllCertificatesChange
                    )
                }
            }

            OutlinedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.section_actions), style = MaterialTheme.typography.titleMedium)
                    ActionButtons(
                        isSaving = state.isSaving,
                        isSending = state.isSendingTest,
                        monitoringEnabled = state.monitoringEnabled,
                        onSave = onSave,
                        onSendTest = onSendTest,
                        onStartMonitoring = onStartMonitoring,
                        onStopMonitoring = onStopMonitoring
                    )

                    AnimatedVisibility(visible = !hasSmsPermission) {
                        Text(
                            text = stringResource(id = R.string.sms_permission_warning),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                val icon = if (monitoringEnabled) Icons.Filled.CheckCircle else Icons.Filled.PauseCircle
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
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ActionButtons(
    isSaving: Boolean,
    isSending: Boolean,
    monitoringEnabled: Boolean,
    onSave: () -> Unit,
    onSendTest: () -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSave,
                enabled = !isSaving && !isSending
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(id = R.string.btn_save))
                }
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onSendTest,
                enabled = !isSending && !isSaving
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(id = R.string.btn_send_test))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onStartMonitoring,
                enabled = !monitoringEnabled && !isSending && !isSaving
            ) {
                Text(stringResource(id = R.string.btn_start))
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onStopMonitoring,
                enabled = monitoringEnabled && !isSaving && !isSending
            ) {
                Text(stringResource(id = R.string.btn_stop))
            }
        }
    }
}
