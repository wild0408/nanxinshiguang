package com.wild0408.nanxinshiguang.data.api.bus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BusRepositoryTest {
    @Test
    fun mapBoundsProjectToImageEdges() {
        val southWest = projectBusCoordinate(LONGITUDE_MIN, LATITUDE_MIN)
        assertEquals(0f, southWest.x)
        assertEquals(1f, southWest.y)
        assertTrue(southWest.inBounds)

        val northEast = projectBusCoordinate(LONGITUDE_MAX, LATITUDE_MAX)
        assertEquals(1f, northEast.x)
        assertEquals(0f, northEast.y)
        assertTrue(northEast.inBounds)
    }

    @Test
    fun coordinatesOutsideMapAreFlaggedInsteadOfPinned() {
        val outside = projectBusCoordinate(120.0, 31.0)
        assertFalse(outside.inBounds, "底图范围外的坐标应被标记越界，而不是钉在边缘伪装成图内车辆")
        assertTrue(outside.x in 0f..1f)
        assertTrue(outside.y in 0f..1f)
    }

    @Test
    fun nonFiniteCoordinatesAreNotInBounds() {
        assertFalse(projectBusCoordinate(Double.NaN, 32.2).inBounds)
    }
}
