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

import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.abs

typealias PathSegments = List<PathSegment>

fun PathSegments.toPath(): Path {
    return Path().apply {
        if (isEmpty()) return@apply
        val startPoint = first().from
        moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
        forEach { it.drawTo(this) }
    }
}

fun PathSegments.toSvg(asDocument: Boolean = false): String = buildString {
    val bounds = this@toSvg.toPath().getBounds()

    if (asDocument) {
        append("""<svg xmlns="http://www.w3.org/2000/svg" """)
        appendLine("""viewBox="${bounds.left} ${bounds.top} ${bounds.width} ${bounds.height}">""")
    }

    if (isNotEmpty()) {
        if (asDocument) {
            append("""  <path d="""")
        }

        val startPoint = this@toSvg.first().from
        append("M${startPoint.x} ${startPoint.y}")

        this@toSvg.forEach { segment ->
            val string = when (segment) {
                is PathSegment.Line -> "L${segment.to.x} ${segment.to.y}"
                is PathSegment.Arc -> {
                    val largeArcFlag = if (abs(segment.sweepAngle) > PI) 1 else 0
                    val sweepFlag = if (segment.sweepAngle > 0) 1 else 0
                    "A${segment.radius} ${segment.radius} 0 $largeArcFlag $sweepFlag ${segment.to.x} ${segment.to.y}"
                }

                is PathSegment.Circle -> {
                    val cx = segment.center.x
                    val cy = segment.center.y
                    val r = segment.radius
                    "M${cx + r} $cy A$r $r 0 1 0 ${cx - r} $cy A$r $r 0 1 0 ${cx + r} $cy Z"
                }

                is PathSegment.Cubic -> "C${segment.p1.x} ${segment.p1.y}, ${segment.p2.x} ${segment.p2.y}, ${segment.p3.x} ${segment.p3.y}"
            }
            append(string)
        }

        if (asDocument) {
            appendLine(""""/>""")
        }
    }
    if (asDocument) {
        appendLine("""</svg>""")
    }
}
