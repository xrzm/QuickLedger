package com.quickledger.app.domain.usecase

import java.util.Calendar
import javax.inject.Inject

class CalculateCycleUseCase @Inject constructor() {

    data class CycleRange(
        val startTime: Long,
        val endTime: Long,
        val label: String
    )

    fun getCurrentCycle(cycleStartDay: Int): CycleRange {
        val now = Calendar.getInstance()
        return getCycleForDate(now, cycleStartDay)
    }

    fun getPreviousCycle(cycleStartDay: Int, currentStart: Long): CycleRange {
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentStart
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return getCycleForDate(cal, cycleStartDay)
    }

    fun getNextCycle(cycleStartDay: Int, currentEnd: Long): CycleRange {
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentEnd
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return getCycleForDate(cal, cycleStartDay)
    }

    fun getCycleForDate(date: Calendar, cycleStartDay: Int): CycleRange {
        val day = clampDay(cycleStartDay)

        val startCal = Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // If current date is before the cycle start, the cycle started last month
        if (date.timeInMillis < startCal.timeInMillis) {
            startCal.add(Calendar.MONTH, -1)
            endCal.add(Calendar.MONTH, -1)
        }

        val label = formatCycleLabel(startCal.timeInMillis, endCal.timeInMillis)
        return CycleRange(startCal.timeInMillis, endCal.timeInMillis, label)
    }

    private fun formatCycleLabel(start: Long, end: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        return "${sdf.format(start)} ~ ${sdf.format(end)}"
    }

    private fun clampDay(day: Int): Int = day.coerceIn(1, 28)
}
