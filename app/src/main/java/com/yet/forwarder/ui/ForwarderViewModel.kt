package com.yet.forwarder.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yet.forwarder.R
import com.yet.forwarder.data.ForwarderSettings
import com.yet.forwarder.data.SettingsStore
import com.yet.forwarder.email.EmailSender
import jakarta.inject.Inject
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ForwarderViewModel @Inject constructor(
    private val app: Application,
    private val settingsStore: SettingsStore,
    private val emailSender: EmailSender
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForwarderUiState())
    val uiState: StateFlow<ForwarderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        smtpServer = settings.smtpServer,
                        portText = settings.port.toString(),
                        username = settings.username,
                        password = settings.password,
                        receiverEmail = settings.receiverEmail,
                        useStartTls = settings.useStartTls,
                        requireAuth = settings.requireAuth,
                        useSsl = settings.useSsl,
                        trustAllCertificates = settings.trustAllCertificates,
                        monitoringEnabled = settings.monitoringEnabled
                    )
                }
            }
        }
    }

    fun onSmtpChanged(value: String) = _uiState.update { it.copy(smtpServer = value) }
    fun onPortChanged(value: String) = _uiState.update { it.copy(portText = value.filter { char -> char.isDigit() }) }
    fun onUsernameChanged(value: String) = _uiState.update { it.copy(username = value) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value) }
    fun onReceiverEmailChanged(value: String) = _uiState.update { it.copy(receiverEmail = value) }
    fun onUseStartTlsChanged(value: Boolean) = _uiState.update { it.copy(useStartTls = value) }
    fun onRequireAuthChanged(value: Boolean) = _uiState.update { it.copy(requireAuth = value) }
    fun onUseSslChanged(value: Boolean) = _uiState.update { it.copy(useSsl = value) }
    fun onTrustAllCertificatesChanged(value: Boolean) = _uiState.update { it.copy(trustAllCertificates = value) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    fun saveSettings() {
        val settings = parseSettings() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val monitoring = _uiState.value.monitoringEnabled
            settingsStore.save(settings.copy(monitoringEnabled = monitoring))
            _uiState.update { it.copy(isSaving = false, message = app.getString(R.string.msg_settings_saved)) }
        }
    }

    fun sendTestEmail() {
        val settings = parseSettings() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingTest = true) }
            runCatching {
                emailSender.sendEmail(
                    settings,
                    app.getString(R.string.email_subject_test),
                    app.getString(R.string.email_body_test)
                )
                val monitoring = _uiState.value.monitoringEnabled
                settingsStore.save(settings.copy(monitoringEnabled = monitoring))
            }.onSuccess {
                _uiState.update { state -> state.copy(message = app.getString(R.string.msg_test_sent)) }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        message = app.getString(
                            R.string.msg_send_failed,
                            throwable.localizedMessage ?: app.getString(R.string.unknown_error)
                        )
                    )
                }
            }
            _uiState.update { it.copy(isSendingTest = false) }
        }
    }

    fun startMonitoring() {
        val settings = parseSettings() ?: return
        viewModelScope.launch {
            settingsStore.save(settings.copy(monitoringEnabled = true))
            _uiState.update {
                it.copy(
                    monitoringEnabled = true,
                    message = app.getString(R.string.msg_monitoring_started)
                )
            }
        }
    }

    fun stopMonitoring() {
        viewModelScope.launch {
            val currentSettings = parseSettings()
                ?: settingsStore.settings.firstOrNull()?.copy(monitoringEnabled = false)
                ?: ForwarderSettings(monitoringEnabled = false)
            settingsStore.save(currentSettings.copy(monitoringEnabled = false))
            _uiState.update {
                it.copy(
                    monitoringEnabled = false,
                    message = app.getString(R.string.msg_monitoring_stopped)
                )
            }
        }
    }

    private fun parseSettings(): ForwarderSettings? {
        val state = _uiState.value
        if (state.smtpServer.isBlank()) {
            _uiState.update { it.copy(message = app.getString(R.string.error_smtp_required)) }
            return null
        }
        val port = state.portText.toIntOrNull()
        if (port == null || port <= 0) {
            _uiState.update { it.copy(message = app.getString(R.string.error_port_invalid)) }
            return null
        }
        if (state.username.isBlank()) {
            _uiState.update { it.copy(message = app.getString(R.string.error_username_required)) }
            return null
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(message = app.getString(R.string.error_password_required)) }
            return null
        }
        if (state.receiverEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(state.receiverEmail).matches()) {
            _uiState.update { it.copy(message = app.getString(R.string.error_receiver_invalid)) }
            return null
        }

        return ForwarderSettings(
            smtpServer = state.smtpServer.trim(),
            port = port,
            username = state.username.trim(),
            password = state.password,
            receiverEmail = state.receiverEmail.trim().lowercase(Locale.getDefault()),
            useStartTls = state.useStartTls,
            requireAuth = state.requireAuth,
            useSsl = state.useSsl,
            trustAllCertificates = state.trustAllCertificates,
            monitoringEnabled = state.monitoringEnabled
        )
    }
}

data class ForwarderUiState(
    val smtpServer: String = "",
    val portText: String = "587",
    val username: String = "",
    val password: String = "",
    val receiverEmail: String = "",
    val useStartTls: Boolean = false,
    val requireAuth: Boolean = false,
    val useSsl: Boolean = false,
    val trustAllCertificates: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val passwordVisible: Boolean = false,
    val isSaving: Boolean = false,
    val isSendingTest: Boolean = false,
    val message: String? = null
)
