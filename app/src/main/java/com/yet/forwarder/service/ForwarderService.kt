package com.yet.forwarder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yet.forwarder.MainActivity
import com.yet.forwarder.R
import com.yet.forwarder.data.SettingsStore
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ForwarderService : Service() {

    private val settingsStore: SettingsStore by inject()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var collectorJob: Job? = null
    private val stopHandled = AtomicBoolean(false)
    private var isForegroundStarted = false
    private var lastCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP && stopHandled.compareAndSet(false, true)) {
            scope.launch {
                settingsStore.setMonitoring(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                isForegroundStarted = false
                stopSelf()
            }
            return START_NOT_STICKY
        }

        if (!isForegroundStarted) {
            startForeground(NOTIFICATION_ID, buildNotification(lastCount))
            isForegroundStarted = true
        }

        if (collectorJob?.isActive != true) {
            collectorJob = scope.launch {
                settingsStore.settings.collectLatest { settings ->
                    if (!settings.monitoringEnabled) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        isForegroundStarted = false
                        stopSelf()
                        return@collectLatest
                    }
                    updateNotification(settings.forwardedCount)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        collectorJob?.cancel()
        scope.cancel()
        isForegroundStarted = false
        super.onDestroy()
    }

    private fun buildNotification(forwardedCount: Int): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ForwarderService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_forwarding_title))
            .setContentText(
                getString(
                    R.string.notification_forwarding_content,
                    forwardedCount
                )
            )
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(
                NotificationCompat.Action(
                    0,
                    getString(R.string.btn_stop),
                    stopIntent
                )
            )
            .build()
    }

    private fun updateNotification(forwardedCount: Int) {
        lastCount = forwardedCount
        if (!isForegroundStarted) {
            startForeground(NOTIFICATION_ID, buildNotification(forwardedCount))
            isForegroundStarted = true
        } else {
            NotificationManagerCompat.from(this).notify(
                NOTIFICATION_ID,
                buildNotification(forwardedCount)
            )
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "forwarder_service_channel"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_STOP = "com.yet.forwarder.action.STOP_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, ForwarderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ForwarderService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
