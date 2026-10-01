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

package com.kyant.capsule

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastCoerceAtLeast

@Stable
fun ContinuousRoundedRectangle.concentricInset(padding: Dp): ContinuousRoundedRectangle {
    return ConcentricContinuousRoundedRectangle(this, padding)
}

@Stable
fun ContinuousRoundedRectangle.concentricOutset(padding: Dp): ContinuousRoundedRectangle {
    return ConcentricContinuousRoundedRectangle(this, -padding)
}

@Stable
fun AbsoluteContinuousRoundedRectangle.concentricInset(padding: Dp): AbsoluteContinuousRoundedRectangle {
    return ConcentricAbsoluteContinuousRoundedRectangle(this, padding)
}

@Stable
fun AbsoluteContinuousRoundedRectangle.concentricOutset(padding: Dp): AbsoluteContinuousRoundedRectangle {
    return ConcentricAbsoluteContinuousRoundedRectangle(this, -padding)
}

@Immutable
private data class ConcentricContinuousRoundedRectangle(
    val containerShape: ContinuousRoundedRectangle,
    val padding: Dp
) : ContinuousRoundedRectangle(
    topStart = ConcentricCornerSize(containerShape.topStart, padding),
    topEnd = ConcentricCornerSize(containerShape.topEnd, padding),
    bottomEnd = ConcentricCornerSize(containerShape.bottomEnd, padding),
    bottomStart = ConcentricCornerSize(containerShape.bottomStart, padding),
    continuity = containerShape.continuity
)

@Immutable
private data class ConcentricAbsoluteContinuousRoundedRectangle(
    val containerShape: AbsoluteContinuousRoundedRectangle,
    val padding: Dp
) : AbsoluteContinuousRoundedRectangle(
    topLeft = ConcentricCornerSize(containerShape.topStart, padding),
    topRight = ConcentricCornerSize(containerShape.topEnd, padding),
    bottomRight = ConcentricCornerSize(containerShape.bottomEnd, padding),
    bottomLeft = ConcentricCornerSize(containerShape.bottomStart, padding),
    continuity = containerShape.continuity
)

@Immutable
private data class ConcentricCornerSize(
    private val containerCornerSize: CornerSize,
    private val padding: Dp
) : CornerSize {

    override fun toPx(shapeSize: Size, density: Density): Float {
        return (containerCornerSize.toPx(shapeSize, density) - with(density) { padding.toPx() })
            .fastCoerceAtLeast(0f)
    }
}
