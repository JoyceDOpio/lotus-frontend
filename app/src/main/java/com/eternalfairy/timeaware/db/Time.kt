package com.eternalfairy.timeaware.db

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class Time(
    var hour: Int = 0,
    var minute: Int = 0
) {
    companion object Companion {
        fun parse(string: String): Time {
            val timeStringParts = string.split(":")
            val hour = timeStringParts[0].toInt()
            val minute = timeStringParts[1].toInt()

            return Time(hour, minute)
        }

        fun parseFromTimestamptz(timestamptz: String): Time {
            // A timestamp with time zone is stored in the "YYYY-MM-DD HH:mm:ss.SSSS+HH:mm" format
            val timeString = timestamptz.split(" ")[1]
            var timeComponents = timeString.split(".")
            timeComponents = timeComponents[0].split(":")
            val hour = timeComponents[0].toInt()
            val minute = timeComponents[1].toInt()

            return Time(hour, minute)
        }
    }

    // return:
    // -1 if this time is earlier than the time it is compared to
    // 0 the times are the same
    // 1 if this time is later than the time it is compared to
    fun compareTo(time: Time): Int {
        return if (this.hour < time.hour) {
            -1
        } else if (this.hour > time.hour) {
            1
        } else {
            if (this.minute < time.minute) {
                -1
            } else if (this.minute > time.minute) {
                1
            } else {
                0
            }
        }
    }

    fun parseToTimestamptz(date: LocalDate): String {
        // A timestamp with time zone is stored in the "YYYY-MM-DD HH:mm:ss.SSSS+HH:mm" format
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-DD")
        val dateString = date.format(formatter)

//        DateTimeFormatter
//            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
//            .withZone(ZoneOffset.UTC)
//            .format(Instant.now())

        val hour = this.hour.toString()
        val minute = this.minute.toString()
        val second = "00.0000"
        val timeString = arrayOf(hour, minute, second).joinToString(":")
        val timestamp = arrayOf(dateString, timeString). joinToString(" ")

        return timestamp + ZoneOffset.UTC
    }

    override fun toString(): String {
        return "%d:%02d".format(hour, minute)
    }
}

