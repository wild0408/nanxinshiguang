package com.wild0408.nanxinshiguang.ui.miuix.hyper.utils

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 越界边缘判定的纯逻辑测试。
 * 触感本身依赖设备振动马达，只能在真机确认，这里只锁定"什么时候该反馈"。
 */
class OverScrollEdgeTest {

    private val trigger = 12f

    @Test
    fun restingOffsetReportNoEdge() {
        assertEquals(OVERSCROLL_EDGE_NONE, resolveOverScrollEdge(offset = 0f, triggerDistance = trigger))
    }

    @Test
    fun offsetBelowTriggerIsIgnored() {
        assertEquals(OVERSCROLL_EDGE_NONE, resolveOverScrollEdge(offset = 11.9f, triggerDistance = trigger))
        assertEquals(OVERSCROLL_EDGE_NONE, resolveOverScrollEdge(offset = -11.9f, triggerDistance = trigger))
    }

    @Test
    fun pullingDownReportsTopEdge() {
        assertEquals(OVERSCROLL_EDGE_TOP, resolveOverScrollEdge(offset = trigger, triggerDistance = trigger))
        assertEquals(OVERSCROLL_EDGE_TOP, resolveOverScrollEdge(offset = 240f, triggerDistance = trigger))
    }

    @Test
    fun pullingUpReportsBottomEdge() {
        assertEquals(OVERSCROLL_EDGE_BOTTOM, resolveOverScrollEdge(offset = -trigger, triggerDistance = trigger))
        assertEquals(OVERSCROLL_EDGE_BOTTOM, resolveOverScrollEdge(offset = -240f, triggerDistance = trigger))
    }
}
