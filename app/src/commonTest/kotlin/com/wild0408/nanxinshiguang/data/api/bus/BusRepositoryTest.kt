package com.wild0408.nanxinshiguang.data.api.bus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BusRepositoryTest {
    @Test
    fun mapBoundsProjectToImageEdges() {
        assertEquals(0f to 1f, projectBusCoordinate(118.704857, 32.197464))
        assertEquals(1f to 0f, projectBusCoordinate(118.728726, 32.208535))
    }

    @Test
    fun coordinatesOutsideMapAreClamped() {
        val (x, y) = projectBusCoordinate(120.0, 31.0)
        assertTrue(x in 0f..1f)
        assertTrue(y in 0f..1f)
    }
}
