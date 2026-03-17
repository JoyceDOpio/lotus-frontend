package com.eternalfairy.timeaware.data.room

import androidx.room.TypeConverter
import com.eternalfairy.timeaware.data.Time
import java.time.LocalDate

public class Converters {
    @TypeConverter
    fun localDateToString(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun stringToLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun timeToString(time: Time): String {
        return time.toString()
    }

    @TypeConverter
    fun stringToTime(timeString: String): Time {
        return Time.Companion.parse(timeString)
    }
}