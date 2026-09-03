package com.papalarm.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.papalarm.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private lateinit var adapter: AlarmAdapter

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        adapter = AlarmAdapter(
            onToggle = { alarm, enabled ->
                AlarmStore.updateEnabled(this, alarm.id, enabled)
                if (enabled) AlarmScheduler.schedule(this, alarm.copy(enabled = true)) else AlarmScheduler.cancel(this, alarm.id)
                refresh()
            },
            onLongClick = { alarm ->
                AlarmScheduler.cancel(this, alarm.id)
                val list = AlarmStore.load(this).filterNot { it.id == alarm.id }
                AlarmStore.save(this, list)
                refresh()
            }
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter
        b.btnAdd.setOnClickListener { startActivity(Intent(this, AddAlarmActivity::class.java)) }
        b.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        requestNeededPermissions()
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val alarms = AlarmStore.load(this)
        adapter.submit(alarms)
        val active = alarms.count { it.enabled }
        b.tvSummary.text = if (alarms.isEmpty()) "🌙 Belum ada yang ngingetin kamu hari ini" else "✅ $active aktif   •   🌷 ${alarms.size} total   •   tahan kartu untuk hapus"
    }

    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                }
            }
        }
    }
}
