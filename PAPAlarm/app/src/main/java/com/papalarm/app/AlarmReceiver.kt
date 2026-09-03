package com.papalarm.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarmId", -1)
        val alarm = AlarmStore.get(context, id) ?: return
        if (!alarm.enabled) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "papalarm_ringing"
        val ch = NotificationChannel(channelId, "Alarm berbunyi", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Alarm PAPAlarm yang sedang berbunyi"
            setSound(null, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(ch)

        val ringIntent = Intent(context, RingActivity::class.java).putExtra("alarmId", id)
        val full = PendingIntent.getActivity(context, id, ringIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.papalarm.app.R.drawable.ic_launcher_pap)
            .setContentTitle("PAPAlarm • ${alarm.label}")
            .setContentText("Belum boleh dimatiin sebelum PAP 😌")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .build()
        nm.notify(id, notification)

        // Also try opening immediately; full-screen notification remains the reliable fallback.
        runCatching { context.startActivity(ringIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

        if (alarm.days.isEmpty()) {
            AlarmStore.updateEnabled(context, id, false)
        } else {
            AlarmScheduler.schedule(context, alarm)
        }
    }
}
