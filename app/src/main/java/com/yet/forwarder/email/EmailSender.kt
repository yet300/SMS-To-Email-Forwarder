package com.yet.forwarder.email

import com.yet.forwarder.data.ForwarderSettings
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailSender {

    suspend fun sendEmail(settings: ForwarderSettings, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            val props = buildProperties(settings)
            val session = Session.getInstance(
                props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(settings.username, settings.password)
                    }
                }
            )

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(settings.username))
                addRecipient(Message.RecipientType.TO, InternetAddress(settings.receiverEmail))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
        }
    }

    private fun buildProperties(settings: ForwarderSettings): Properties {
        return Properties().apply {
            put("mail.transport.protocol", "smtp")
            put("mail.smtp.host", settings.smtpServer)
            put("mail.smtp.port", settings.port.toString())

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
}
