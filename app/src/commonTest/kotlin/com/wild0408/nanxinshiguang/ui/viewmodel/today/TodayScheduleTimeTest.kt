package com.wild0408.nanxinshiguang.ui.viewmodel.today

import com.wild0408.nanxinshiguang.data.db.main.Course
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 今日课表"课程是否已结束"的判断。
 * Material 与 Miuix 两套界面共用该实现，避免两边样式判定再次走样。
 */
class TodayScheduleTimeTest {

    private fun course(endTime: String?) = Course(
        id = "c1",
        courseTableId = "t1",
        name = "大学物理",
        teacher = "王教授",
        position = "物理实验室",
        day = 1,
        startSection = null,
        endSection = null,
        isCustomTime = true,
        customStartTime = "08:00",
        customEndTime = endTime,
        colorInt = 0,
    )

    private fun model(endTime: String?) = CourseDisplayModel(
        course = course(endTime),
        startTime = "08:00",
        endTime = endTime,
    )

    @Test
    fun finishedOnlyAfterEndTimeHasPassed() {
        assertTrue(model("10:00").isFinishedAt(LocalTime(10, 1)), "下课后应判为已结束")
        assertTrue(model("10:00").isFinishedAt(LocalTime(23, 59)), "当天晚些时候应判为已结束")
        // 与下课时间相同仍算"在上课"，与原实现（end < now）一致
        assertFalse(model("10:00").isFinishedAt(LocalTime(10, 0)), "正好下课时刻不算已结束")
        assertFalse(model("10:00").isFinishedAt(LocalTime(9, 59)), "上课中不应判为已结束")
        assertFalse(model("23:59").isFinishedAt(LocalTime(0, 1)), "跨天时段不应误判")
    }

    @Test
    fun missingOrInvalidEndTimeIsTreatedAsUnfinished() {
        assertFalse(model(null).isFinishedAt(LocalTime(23, 59)), "缺少结束时间不应判为已结束")
        assertFalse(model("").isFinishedAt(LocalTime(23, 59)), "空结束时间不应判为已结束")
        assertFalse(model("   ").isFinishedAt(LocalTime(23, 59)), "空白结束时间不应判为已结束")
        assertFalse(model("25:99").isFinishedAt(LocalTime(23, 59)), "非法时间不应判为已结束")
        assertFalse(model("十点").isFinishedAt(LocalTime(23, 59)), "无法解析的时间不应判为已结束")
        assertFalse(model("10:00 ").isFinishedAt(LocalTime(9, 0)), "带空格的合法时间应能解析")
        assertTrue(model("10:00 ").isFinishedAt(LocalTime(11, 0)), "带空格的合法时间应能解析")
    }

    @Test
    fun placeholderIsNotATime() {
        assertEquals("--:--", TIME_PLACEHOLDER)
        assertFalse(model(TIME_PLACEHOLDER).isFinishedAt(LocalTime(23, 59)), "占位符不应被解析成时间")
    }
}
