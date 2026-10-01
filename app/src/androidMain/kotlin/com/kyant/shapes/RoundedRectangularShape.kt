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

package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * fork 自 io.github.kyant0:shapes 的 RoundedRectangularShape，
 * 仅保留 backdrop 的 Lens 效果所需的最小接口（corners）。
 */
@Immutable
interface RoundedRectangularShape : Shape {

    fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Corners

    data class Corners(
        val topLeft: Float,
        val topRight: Float,
        val bottomRight: Float,
        val bottomLeft: Float
    )
}