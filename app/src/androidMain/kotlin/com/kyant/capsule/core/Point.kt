/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Ported into Nanxin Shiguang (NUIST edition) from
   https://github.com/Kyant0/AndroidLiquidGlass
   (io.github.kyant0:backdrop, io.github.kyant0:shapes).
   See the repository NOTICE file.
 */

package com.kyant.capsule.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kyant.capsule.lerp
import kotlin.math.sqrt

@Immutable
data class Point(val x: Double, val y: Double) {

    @Stable
    operator fun unaryMinus(): Point {
        return Point(-x, -y)
    }

    @Stable
    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    @Stable
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    @Stable
    operator fun times(operand: Double): Point {
        return Point(x * operand, y * operand)
    }

    @Stable
    operator fun div(operand: Double): Point {
        return Point(x / operand, y / operand)
    }

    @Stable
    fun normalized(): Point {
        val length = sqrt(x * x + y * y)
        return if (length != 0.0) this / length else Zero
    }

    companion object {

        @Stable
        val Zero: Point = Point(0.0, 0.0)
    }
}

@Stable
fun lerp(start: Point, stop: Point, fraction: Double): Point {
    return Point(
        lerp(start.x, stop.x, fraction),
        lerp(start.y, stop.y, fraction)
    )
}
