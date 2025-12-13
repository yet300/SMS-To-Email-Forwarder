package com.yet.forwarder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import jakarta.inject.Singleton
import jakarta.inject.Inject
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

val Context.forwarderDataStore: DataStore<Preferences> by preferencesDataStore(name = "forwarder_settings")

@Singleton
@Single
class SettingsStore @Inject constructor(private val context: Context) {

    private val dataStore = context.forwarderDataStore

    val settings: Flow<ForwarderSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ForwarderSettings(
                smtpServer = preferences[Keys.SMTP_SERVER] ?: "",
                port = preferences[Keys.PORT] ?: 587,
                username = preferences[Keys.USERNAME] ?: "",
                password = preferences[Keys.PASSWORD] ?: "",
                receiverEmail = preferences[Keys.RECEIVER_EMAIL] ?: "",
                useStartTls = preferences[Keys.USE_STARTTLS] ?: false,
                requireAuth = preferences[Keys.REQUIRE_AUTH] ?: false,
                useSsl = preferences[Keys.USE_SSL] ?: false,
                trustAllCertificates = preferences[Keys.TRUST_ALL_CERTIFICATES] ?: false,
                monitoringEnabled = preferences[Keys.MONITORING_ENABLED] ?: false
            )
        }

    suspend fun save(settings: ForwarderSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SMTP_SERVER] = settings.smtpServer
            preferences[Keys.PORT] = settings.port
            preferences[Keys.USERNAME] = settings.username
            preferences[Keys.PASSWORD] = settings.password
            preferences[Keys.RECEIVER_EMAIL] = settings.receiverEmail
            preferences[Keys.USE_STARTTLS] = settings.useStartTls
            preferences[Keys.REQUIRE_AUTH] = settings.requireAuth
            preferences[Keys.USE_SSL] = settings.useSsl
            preferences[Keys.TRUST_ALL_CERTIFICATES] = settings.trustAllCertificates
            preferences[Keys.MONITORING_ENABLED] = settings.monitoringEnabled
        }
    }

    private object Keys {
        val SMTP_SERVER = stringPreferencesKey("smtp_server")
        val PORT = intPreferencesKey("port")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val RECEIVER_EMAIL = stringPreferencesKey("receiver_email")
        val USE_STARTTLS = booleanPreferencesKey("use_starttls")
        val REQUIRE_AUTH = booleanPreferencesKey("require_auth")
        val USE_SSL = booleanPreferencesKey("use_ssl")
        val TRUST_ALL_CERTIFICATES = booleanPreferencesKey("trust_all_certificates")
        val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
    }
}
