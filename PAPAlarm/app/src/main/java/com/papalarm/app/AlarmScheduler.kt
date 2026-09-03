package com.papalarm.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {
    fun schedule(context: Context, alarm: AlarmItem) {
        if (!alarm.enabled) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = nextTrigger(alarm)
        val pi = pendingIntent(context, alarm.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, id))
    }

    fun nextTrigger(a: AlarmItem): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, a.hour); set(Calendar.MINUTE, a.minute)
        }
        if (a.days.isEmpty()) {
            if (candidate.timeInMillis <= now.timeInMillis) candidate.add(Calendar.DAY_OF_YEAR, 1)
            return candidate.timeInMillis
        }
        for (offset in 0..7) {
            val c = candidate.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, offset)
            if (c.get(Calendar.DAY_OF_WEEK) in a.days && c.timeInMillis > now.timeInMillis) return c.timeInMillis
        }
        candidate.add(Calendar.DAY_OF_YEAR, 7)
        return candidate.timeInMillis
    }

    private fun pendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra("alarmId", id)
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
