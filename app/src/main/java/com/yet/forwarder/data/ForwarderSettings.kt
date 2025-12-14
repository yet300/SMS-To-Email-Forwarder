package com.yet.forwarder.data

data class ForwarderSettings(
    val smtpServer: String = "",
    val port: Int = 587,
    val username: String = "",
    val password: String = "",
    val receiverEmail: String = "",
    val useStartTls: Boolean = false,
    val requireAuth: Boolean = false,
    val useSsl: Boolean = false,
    val trustAllCertificates: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val forwardedCount: Int = 0
)
