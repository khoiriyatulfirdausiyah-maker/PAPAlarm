package com.papalarm.app

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.papalarm.app.databinding.ActivityAddAlarmBinding
import java.io.File
import java.util.Calendar

class AddAlarmActivity : AppCompatActivity() {
    private lateinit var b: ActivityAddAlarmBinding
    private var selectedAudio: String? = null
    private var recorder: MediaRecorder? = null
    private var recording = false
    private var recordFile: File? = null

    private val audioPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        selectedAudio = uri.toString(); b.tvAudio.text = "Suara: audio pilihan 🎵"
    }
    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startRecording() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddAlarmBinding.inflate(layoutInflater); setContentView(b.root)
        b.spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Self Care","Ibadah","Kesehatan","Rumah","Belajar","Hewan","Lainnya"))
        b.btnPickAudio.setOnClickListener { audioPicker.launch(arrayOf("audio/*")) }
        b.btnRecord.setOnClickListener { if (recording) stopRecording() else micPermission.launch(Manifest.permission.RECORD_AUDIO) }
        b.btnSave.setOnClickListener { saveAlarm() }
    }

    private fun startRecording() {
        recordFile = File(filesDir, "voice_${System.currentTimeMillis()}.m4a")
        recorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordFile!!.absolutePath)
            prepare(); start()
        }
        recording = true; b.btnRecord.text = "⏹ Selesai merekam"
        b.tvAudio.text = "Sedang merekam... ngomong sesukamu 😭"
    }

    private fun stopRecording() {
        runCatching { recorder?.stop() }
        recorder?.release(); recorder = null; recording = false
        selectedAudio = Uri.fromFile(recordFile).toString()
        b.btnRecord.text = "🎙 Rekam ulang suara"
        b.tvAudio.text = "Suara: rekamanmu sendiri 🎙"
    }

    private fun saveAlarm() {
        if (recording) stopRecording()
        val label = b.etLabel.text.toString().trim().ifBlank { "Rutinitas" }
        val dayViews = listOf(b.d1,b.d2,b.d3,b.d4,b.d5,b.d6,b.d7)
        val dayVals = listOf(Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY,Calendar.SUNDAY)
        val days = dayViews.mapIndexedNotNull { i, cb -> dayVals[i].takeIf { cb.isChecked } }.toSet()
        val alarm = AlarmItem(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), hour = b.timePicker.hour, minute = b.timePicker.minute,
            label = label, category = b.spCategory.selectedItem.toString(), days = days, enabled = true,
            audioUri = selectedAudio, vibrate = b.cbVibrate.isChecked
        )
        val list = AlarmStore.load(this); list.add(alarm); AlarmStore.save(this, list); AlarmScheduler.schedule(this, alarm)
        Toast.makeText(this, "Alarm tersimpan ✨", Toast.LENGTH_SHORT).show(); finish()
    }

    override fun onDestroy() { if (recording) stopRecording(); super.onDestroy() }
}
