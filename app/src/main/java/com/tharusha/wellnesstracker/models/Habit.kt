package com.tharusha.wellnesstracker.models

import java.text.SimpleDateFormat
import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var targetCount: Int = 1,
    var currentCount: Int = 0,
    var completed: Boolean = false,
    var date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)