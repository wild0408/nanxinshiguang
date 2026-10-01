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

package com.kyant.capsule.path

import com.kyant.capsule.core.Point

inline fun buildPathSegments(block: PathSegmentsBuilder.() -> Unit): PathSegments {
    return PathSegmentsBuilder().apply(block).build()
}

fun buildCirclePathSegments(center: Point, radius: Double): PathSegments {
    return listOf(PathSegment.Circle(center, radius))
}

class PathSegmentsBuilder {

    private var startPoint = Point.Zero
    private var currentPoint = Point.Zero
    private var didMove = false

    private var segments = mutableListOf<PathSegment>()

    fun moveTo(x: Double, y: Double) {
        if (didMove) {
            throw IllegalStateException("moveTo can only be called once at the beginning of the path")
        }
        didMove = true
        startPoint = Point(x, y)
        currentPoint = startPoint
    }

    fun lineTo(x: Double, y: Double) {
        val segment = PathSegment.Line(currentPoint, Point(x, y))
        segments += segment
        currentPoint = segment.to
    }

    fun arcTo(center: Point, radius: Double, startAngle: Double, sweepAngle: Double) {
        val segment = PathSegment.Arc(center, radius, startAngle, sweepAngle)
        segments += segment
        currentPoint = segment.to
    }

    fun cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double) {
        val segment = PathSegment.Cubic(
            currentPoint,
            Point(x1, y1),
            Point(x2, y2),
            Point(x3, y3)
        )
        segments += segment
        currentPoint = segment.to
    }

    fun close() {
        val segment = PathSegment.Line(currentPoint, startPoint)
        segments += segment
        currentPoint = segment.to
    }

    fun build(): PathSegments {
        return segments.toList()
    }
}
