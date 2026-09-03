package com.papalarm.app

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val category: String,
    val days: Set<Int>,
    var enabled: Boolean = true,
    val audioUri: String? = null,
    val vibrate: Boolean = true
)
