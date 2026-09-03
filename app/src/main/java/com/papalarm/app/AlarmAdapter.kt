package com.papalarm.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.papalarm.app.databinding.ItemAlarmBinding
import java.util.Calendar
import java.util.Locale

class AlarmAdapter(
    private val onToggle: (AlarmItem, Boolean) -> Unit,
    private val onLongClick: (AlarmItem) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.Holder>() {
    private val data = mutableListOf<AlarmItem>()
    fun submit(items: List<AlarmItem>) { data.clear(); data.addAll(items); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(data[position])

    inner class Holder(private val b: ItemAlarmBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(a: AlarmItem) {
            b.tvIcon.text = emoji(a.category)
            b.tvTime.text = String.format(Locale.getDefault(), "%02d:%02d", a.hour, a.minute)
            b.tvLabel.text = a.label
            b.tvDays.text = daysText(a.days)
            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.isChecked = a.enabled
            b.switchEnabled.setOnCheckedChangeListener { _, checked -> onToggle(a, checked) }
            b.root.setOnLongClickListener { onLongClick(a); true }
        }
    }

    private fun emoji(c: String) = when(c) {
        "Self Care" -> "🧴"; "Ibadah" -> "📖"; "Kesehatan" -> "💗"; "Rumah" -> "🏡"; "Belajar" -> "📚"; "Hewan" -> "🐾"; else -> "✨"
    }
    private fun daysText(d: Set<Int>): String {
        if (d.isEmpty()) return "Sekali"
        if (d.size == 7) return "Setiap hari"
        val names = mapOf(Calendar.MONDAY to "Sen", Calendar.TUESDAY to "Sel", Calendar.WEDNESDAY to "Rab", Calendar.THURSDAY to "Kam", Calendar.FRIDAY to "Jum", Calendar.SATURDAY to "Sab", Calendar.SUNDAY to "Min")
        return listOf(Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY,Calendar.SUNDAY).filter { it in d }.joinToString(", ") { names[it]!! }
    }
}
