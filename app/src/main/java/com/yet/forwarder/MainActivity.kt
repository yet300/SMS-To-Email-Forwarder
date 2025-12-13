package com.yet.forwarder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yet.forwarder.R
import com.yet.forwarder.ui.ForwarderScreen
import com.yet.forwarder.ui.ForwarderViewModel
import com.yet.forwarder.ui.theme.SMSToEmailForwarderTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMSToEmailForwarderTheme {
                val viewModel: ForwarderViewModel = viewModel(factory = ForwarderViewModel.Factory)
                val state by viewModel.uiState.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                var hasSmsPermission by rememberSaveable { mutableStateOf(hasSmsPermission()) }
                var startRequested by rememberSaveable { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    hasSmsPermission = granted
                    if (granted && startRequested) {
                        viewModel.startMonitoring()
                    } else if (startRequested) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(getString(R.string.permission_sms_required))
                        }
                    }
                    startRequested = false
                }

                ForwarderScreen(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    hasSmsPermission = hasSmsPermission,
                    onSmtpChange = viewModel::onSmtpChanged,
                    onPortChange = viewModel::onPortChanged,
                    onUsernameChange = viewModel::onUsernameChanged,
                    onPasswordChange = viewModel::onPasswordChanged,
                    onReceiverEmailChange = viewModel::onReceiverEmailChanged,
                    onUseStartTlsChange = viewModel::onUseStartTlsChanged,
                    onRequireAuthChange = viewModel::onRequireAuthChanged,
                    onUseSslChange = viewModel::onUseSslChanged,
                    onTrustAllCertificatesChange = viewModel::onTrustAllCertificatesChanged,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onSave = viewModel::saveSettings,
                    onSendTest = viewModel::sendTestEmail,
                    onStartMonitoring = {
                        if (hasSmsPermission) {
                            viewModel.startMonitoring()
                        } else {
                            startRequested = true
                            permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        }
                    },
                    onStopMonitoring = { viewModel.stopMonitoring() },
                    onMessageConsumed = viewModel::consumeMessage
                )

                LaunchedEffect(state.monitoringEnabled) {
                    if (state.monitoringEnabled && !hasSmsPermission) {
                        hasSmsPermission = hasSmsPermission()
                    }
                }
            }
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
