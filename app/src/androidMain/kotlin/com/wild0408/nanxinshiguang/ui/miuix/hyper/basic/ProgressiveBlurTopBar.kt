package com.wild0408.nanxinshiguang.ui.miuix.hyper.basic

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.runtimeShaderEffect
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.concurrent.atomic.AtomicInteger

private val progressiveBlurShaderSequence = AtomicInteger(0)

/** Backdrop 渐进模糊层，参数和采样结构对齐参考项目，同时保留动态顶栏高度。 */
@Composable
fun ProgressiveBlurTopBar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified,
    tintIntensity: Float = 0.2f,
    tintColor: Color = MiuixTheme.colorScheme.surface,
    blurAlpha: Float = 1f,
    edgeFadeStart: Float = 0.94f,
    content: @Composable BoxScope.() -> Unit,
) {
    val shaderKey = remember {
        "HyperProgressiveBlur_${progressiveBlurShaderSequence.incrementAndGet()}"
    }
    val denoiseKey = remember(shaderKey) { "${shaderKey}_denoise" }
    val blurEffects: com.kyant.backdrop.BackdropEffectScope.() -> Unit = remember(
        shaderKey, denoiseKey, tintColor, tintIntensity, edgeFadeStart,
    ) {
        {
            val maxRadiusPx = 12f.dp.toPx()
            padding = maxRadiusPx
            val pad = padding * downsampleScale
            val contentW = size.width * downsampleScale
            val contentH = size.height * downsampleScale
            val bufferW = contentW + 2f * pad
            val bufferH = contentH + 2f * pad
            runtimeShaderEffect(shaderKey, PROGRESSIVE_BLUR_SHADER, "content") {
                setFloatUniform("contentOrigin", pad, pad)
                setFloatUniform("contentSize", contentW, contentH)
                setFloatUniform("bufferSize", bufferW, bufferH)
                setFloatUniform("maxRadius", maxRadiusPx * downsampleScale)
                setFloatUniform("edgeFadeStart", edgeFadeStart)
                setColorUniform("tint", tintColor)
                setFloatUniform("tintIntensity", tintIntensity)
            }
            runtimeShaderEffect(denoiseKey, PROGRESSIVE_DENOISE_SHADER, "content") {
                setFloatUniform("contentOrigin", pad, pad)
                setFloatUniform("contentSize", contentW, contentH)
                setFloatUniform("bufferSize", bufferW, bufferH)
                setFloatUniform("maxRadius", maxRadiusPx * downsampleScale)
            }
        }
    }
    val sizeModifier = if (height == Dp.Unspecified) Modifier else Modifier.fillMaxWidth().height(height)
    val backdropModifier = if (Build.VERSION.SDK_INT >= 33) {
        Modifier
            .graphicsLayer { alpha = blurAlpha }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = blurEffects,
                highlight = null,
                shadow = null,
                downsampleScale = 1f,
            )
    } else {
        val gradientColor = MiuixTheme.colorScheme.surface
        Modifier
            .graphicsLayer { alpha = blurAlpha }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to gradientColor.copy(alpha = 0.9f),
                        0.4f to gradientColor.copy(alpha = 0.82f),
                        0.7f to gradientColor.copy(alpha = 0.6f),
                        1.0f to Color.Transparent,
                    ),
                ),
            )
    }
    // 模糊作为顶栏节点自身的背景绘制，不创建参与测量的铺满子节点。
    // 因此 Scaffold 只看到 Miuix TopAppBar 的真实动态高度。
    Box(
        modifier = modifier.then(sizeModifier).then(backdropModifier),
        content = content,
    )
}

private const val SOFTER_STEP = """
float softerstep(float a, float b, float x) {
    float s = clamp((x - a) / max(b - a, 0.0001), 0.0, 1.0);
    return s * s * s * (s * (s * 6.0 - 15.0) + 10.0);
}
"""

private const val PROGRESSIVE_BLUR_SHADER = """
uniform shader content;
uniform float2 contentOrigin;
uniform float2 contentSize;
uniform float2 bufferSize;
uniform float maxRadius;
uniform float edgeFadeStart;
layout(color) uniform half4 tint;
uniform float tintIntensity;
$SOFTER_STEP
float hash12(float2 p) {
    float3 p3 = fract(float3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}
half4 progressiveBlur(float2 coord, float radius) {
    if (radius < 0.5) return content.eval(coord);
    float h = hash12(coord);
    float2 dir = float2(cos(h * 6.2831853), sin(h * 6.2831853));
    float2 g = float2(cos(2.39996323), sin(2.39996323));
    half4 sum = half4(0.0);
    float wsum = 0.0;
    for (int i = 0; i < 32; i++) {
        float fi = float(i);
        float ff = (fi + 0.5) / 32.0;
        float r = radius * sqrt(ff);
        r *= 0.90 + 0.20 * fract(h * 93.9898 + fi * 0.7548776662);
        float w = exp(-ff / 0.85);
        half4 c = content.eval(clamp(coord + dir * r, float2(0.0), max(bufferSize - 1.0, float2(0.0))));
        if (c.a > 0.02) { sum += c * w; wsum += w; }
        dir = float2(dir.x * g.x - dir.y * g.y, dir.x * g.y + dir.y * g.x);
    }
    return wsum < 0.0001 ? content.eval(coord) : sum / wsum;
}
half4 main(float2 coord) {
    float2 local = coord - contentOrigin;
    if (local.x < 0.0 || local.y < 0.0 || local.x >= contentSize.x || local.y >= contentSize.y) return content.eval(coord);
    float t = clamp(local.y / max(contentSize.y, 1.0), 0.0, 1.0);
    // Keep a small blur contribution into the lower edge. Applying the
    // falloff over a slightly longer virtual range avoids an early clear band
    // while the actual top-bar measurement remains unchanged.
    float blurT = clamp(t / 1.12, 0.0, 1.0);
    float u = 1.0 - smoothstep(0.0, 1.0, blurT);
    float edge = softerstep(edgeFadeStart, 1.0, t);
    half4 color = progressiveBlur(coord, maxRadius * u) * (1.0 - edge);
    if (tintIntensity > 0.0) color = mix(color, tint * (1.0 - edge), tintIntensity * u);
    return color;
}
"""

private const val PROGRESSIVE_DENOISE_SHADER = """
uniform shader content;
uniform float2 contentOrigin;
uniform float2 contentSize;
uniform float2 bufferSize;
uniform float maxRadius;
half4 main(float2 coord) {
    float2 local = coord - contentOrigin;
    if (local.x < 0.0 || local.y < 0.0 || local.x >= contentSize.x || local.y >= contentSize.y) return content.eval(coord);
    float t = clamp(local.y / max(contentSize.y, 1.0), 0.0, 1.0);
    float blurT = clamp(t / 1.12, 0.0, 1.0);
    float r = maxRadius * (1.0 - smoothstep(0.0, 1.0, blurT)) * 0.18;
    if (r < 0.4) return content.eval(coord);
    half4 sum = content.eval(coord);
    float wsum = 1.0;
    float2 dir = float2(1.0, 0.0);
    float2 g = float2(cos(0.7853981634), sin(0.7853981634));
    for (int i = 0; i < 8; i++) {
        half4 c = content.eval(clamp(coord + dir * r, float2(0.0), max(bufferSize - 1.0, float2(0.0))));
        if (c.a > 0.02) { sum += c; wsum += 1.0; }
        dir = float2(dir.x * g.x - dir.y * g.y, dir.x * g.y + dir.y * g.x);
    }
    return sum / wsum;
}
"""
