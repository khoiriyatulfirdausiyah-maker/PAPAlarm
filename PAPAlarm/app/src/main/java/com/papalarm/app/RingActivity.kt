package com.papalarm.app

import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.papalarm.app.databinding.ActivityRingBinding
import java.util.Locale

class RingActivity : AppCompatActivity() {
    private lateinit var b: ActivityRingBinding
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var alarmId: Int = -1

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) finishAlarm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true); setTurnScreenOn(true)
        b = ActivityRingBinding.inflate(layoutInflater); setContentView(b.root)
        alarmId = intent.getIntExtra("alarmId", -1)
        val alarm = AlarmStore.get(this, alarmId)
        if (alarm == null) { finish(); return }
        b.tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)
        b.tvLabel.text = "Waktunya ${alarm.label} ✨"
        b.tvEmoji.text = when(alarm.category) { "Self Care" -> "🧴"; "Ibadah" -> "📖"; "Kesehatan" -> "💗"; "Rumah" -> "🏡"; "Belajar" -> "📚"; "Hewan" -> "🐾"; else -> "🌷" }
        b.btnPap.setOnClickListener { takePhoto.launch(null) }
        startSound(alarm)
    }

    private fun startSound(a: AlarmItem) {
        val uri = a.audioUri?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            setDataSource(this@RingActivity, uri)
            isLooping = true
            prepare(); start()
        }
        if (a.vibrate) {
            vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator else @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,350,500), 0))
        }
    }

    private fun finishAlarm() {
        player?.stop(); player?.release(); player = null
        vibrator?.cancel()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(alarmId)
        b.tvLabel.text = "Beres! Alarm dimatikan 💗"
        finish()
    }

    override fun onDestroy() {
        player?.release(); vibrator?.cancel(); super.onDestroy()
    }

    override fun onBackPressed() { /* sengaja tidak bisa ditutup dengan tombol Back */ }
}
