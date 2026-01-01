package com.yet.forwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import com.yet.forwarder.R
import com.yet.forwarder.data.ForwarderSettings
import com.yet.forwarder.data.SettingsStore
import com.yet.forwarder.email.EmailSender
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver @JvmOverloads  @Inject constructor(
    private val settingsStore: SettingsStore = get(SettingsStore::class.java),
    private val emailSender: EmailSender = get(EmailSender::class.java)
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) {
            pendingResult.finish()
            return
        }

        val sender = messages.first().displayOriginatingAddress.orEmpty()
        val body = messages.joinToString(separator = "") { it.displayMessageBody }
        val timestamp = SimpleDateFormat("E dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsStore.settings.first()
                if (!settings.monitoringEnabled) {
                    Log.i(TAG, "Monitoring disabled. SMS ignored.")
                    return@launch
                }
                if (!settings.isConfigured()) {
                    Log.w(TAG, "Missing SMTP configuration. SMS ignored.")
                    return@launch
                }

                val subject = appContext.getString(R.string.email_subject_format, timestamp, sender)
                emailSender.sendEmail(settings, subject, body)
                settingsStore.incrementForwardedCount()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(appContext, appContext.getString(R.string.sms_forwarded_to_email), Toast.LENGTH_SHORT).show()
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to forward SMS", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun ForwarderSettings.isConfigured(): Boolean {
        return smtpServer.isNotBlank() &&
            port > 0 &&
            username.isNotBlank() &&
            password.isNotBlank() &&
            receiverEmail.isNotBlank()
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
