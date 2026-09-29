package com.wild0408.nanxinshiguang.tool

import com.wild0408.nanxinshiguang.data.db.main.CourseWithWeeks
import com.wild0408.nanxinshiguang.data.db.main.TimeSlot
import kotlinx.datetime.LocalDate

expect object CalendarAccountManager {
    /**
     * 将当前课表同步到系统日历
     */
    suspend fun syncCurrentTableToSystemCalendar(
        courses: List<CourseWithWeeks>,
        getTimeSlotsForDate: suspend (LocalDate) -> List<TimeSlot>,
        semesterStartDate: LocalDate,
        semesterTotalWeeks: Int,
        firstDayOfWeekInt: Int,
        alarmMinutes: Int?,
        skippedDates: Set<String>?
    ): Boolean
}