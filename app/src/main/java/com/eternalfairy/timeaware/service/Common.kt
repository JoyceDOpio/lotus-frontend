package com.eternalfairy.timeaware.service

fun formatTime(hours: String, minutes: String, seconds: String): String = "$hours:$minutes:$seconds"

fun Int.pad(): String = this.toString().padStart(2,'0')