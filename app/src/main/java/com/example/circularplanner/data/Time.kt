package com.example.circularplanner.data

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
    }

    override fun toString(): String {
        return "%d:%02d".format(hour, minute)
    }
}

