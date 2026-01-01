package com.yet.forwarder.email

import com.yet.forwarder.data.ForwarderSettings
import jakarta.inject.Singleton
import java.net.ConnectException
import java.net.Inet4Address
import java.net.InetAddress
import java.util.Properties
import android.util.Log
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class EmailSender {

    suspend fun sendEmail(settings: ForwarderSettings, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            sendWithFallback(settings, subject, body)
        }
    }

    private fun sendWithFallback(settings: ForwarderSettings, subject: String, body: String) {
        val props = buildProperties(settings, null)
        val session = buildSession(settings, props)
        val message = buildMessage(session, settings, subject, body)

        try {
            Transport.send(message)
        } catch (exception: Exception) {
            if (!isConnectFailure(exception)) {
                throw exception
            }

            val ipv4Host = resolveIpv4Host(settings.smtpServer)
            if (ipv4Host == null || ipv4Host == settings.smtpServer) {
                Log.w(TAG, "SMTP connect failed; no IPv4 fallback available", exception)
                throw exception
            }

            Log.w(TAG, "SMTP connect failed; retrying with IPv4 host $ipv4Host", exception)
            val retryProps = buildProperties(settings, ipv4Host)
            val retrySession = buildSession(settings, retryProps)
            val retryMessage = buildMessage(retrySession, settings, subject, body)
            Transport.send(retryMessage)
        }
    }

    private fun buildSession(settings: ForwarderSettings, props: Properties): Session {
        return Session.getInstance(
            props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(settings.username, settings.password)
                }
            }
        )
    }

    private fun buildMessage(
        session: Session,
        settings: ForwarderSettings,
        subject: String,
        body: String
    ): MimeMessage {
        return MimeMessage(session).apply {
            setFrom(InternetAddress(settings.username))
            addRecipient(Message.RecipientType.TO, InternetAddress(settings.receiverEmail))
            setSubject(subject)
            setText(body)
        }
    }

    private fun buildProperties(settings: ForwarderSettings, hostOverride: String?): Properties {
        return Properties().apply {
            put("mail.transport.protocol", "smtp")
            put("mail.smtp.host", hostOverride ?: settings.smtpServer)
            put("mail.smtp.port", settings.port.toString())
            put("mail.smtp.connectiontimeout", CONNECT_TIMEOUT_MS.toString())
            put("mail.smtp.timeout", IO_TIMEOUT_MS.toString())
            put("mail.smtp.writetimeout", IO_TIMEOUT_MS.toString())

            if (settings.useStartTls) {
                put("mail.smtp.starttls.enable", "true")
            }
            if (settings.requireAuth) {
                put("mail.smtp.auth", "true")
            }
            if (settings.useSsl) {
                put("mail.smtp.socketFactory.port", settings.port.toString())
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.ssl.enable", "true")
            }
            if (settings.trustAllCertificates) {
                put("mail.smtp.ssl.trust", "*")
            }
        }
    }

    private fun resolveIpv4Host(host: String): String? {
        return try {
            InetAddress.getAllByName(host)
                .firstOrNull { it is Inet4Address }
                ?.hostAddress
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to resolve IPv4 for $host", exception)
            null
        }
    }

    private fun isConnectFailure(exception: Throwable): Boolean {
        var current: Throwable? = exception
        while (current != null) {
            if (current is ConnectException) {
                return true
            }
            if (current.javaClass.name == MAIL_CONNECT_EXCEPTION) {
                return true
            }
            current = current.cause
        }
        return false
    }

    companion object {
        private const val TAG = "EmailSender"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val IO_TIMEOUT_MS = 20000
        private const val MAIL_CONNECT_EXCEPTION = "com.sun.mail.util.MailConnectException"
    }
}
