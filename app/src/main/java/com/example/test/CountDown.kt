package com.example.test

import android.icu.util.Calendar

fun getNextTargetTimeMillis(currentTimeMillis: Long, targetTimes: List<String>): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTimeMillis

    var nextTargetMillis = Long.MAX_VALUE
    for (targetTime in targetTimes) {
        val parts = targetTime.split(":")
        val targetHour = parts[0].toInt()
        val targetMinute = parts[1].toInt()

        val tempCalendar = calendar.clone() as Calendar
        if (tempCalendar.get(Calendar.HOUR_OF_DAY) > targetHour ||
            (tempCalendar.get(Calendar.HOUR_OF_DAY) == targetHour &&
                    tempCalendar.get(Calendar.MINUTE) >= targetMinute)
        ) {
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        tempCalendar.set(Calendar.HOUR_OF_DAY, targetHour)
        tempCalendar.set(Calendar.MINUTE, targetMinute)
        tempCalendar.set(Calendar.SECOND, 0)
        tempCalendar.set(Calendar.MILLISECOND, 0)

        val targetMillis = tempCalendar.timeInMillis
        if (targetMillis in (currentTimeMillis + 1)..<nextTargetMillis) {
            nextTargetMillis = targetMillis
        }
    }
    return nextTargetMillis
}