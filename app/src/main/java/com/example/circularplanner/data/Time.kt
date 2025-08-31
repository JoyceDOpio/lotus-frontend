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

    // return:
    // -1 if this time is earlier than the time it is compared to
    // 0 the times are the same
    // 1 if this time is later than the time it is compared to
    fun compareTo(time: Time): Int {
        if (this.hour < time.hour) {
            return -1
        } else if (this.hour > time.hour) {
            return 1
        } else {
            if (this.minute < time.minute) {
                return -1
            } else if (this.minute > time.minute) {
                return 1
            } else {
                return 0
            }
        }
    }

    override fun toString(): String {
        return "%d:%02d".format(hour, minute)
    }
}

