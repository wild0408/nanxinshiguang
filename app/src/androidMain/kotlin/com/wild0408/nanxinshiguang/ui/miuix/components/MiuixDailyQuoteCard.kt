package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.model.DailyQuote
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_refresh_daily_quote
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 「每日一言」卡片（Miuix）。
 *
 * 用官方 `Surface(onClick = ...)` 承载点击，从而获得 Miuix 默认的按压反馈
 * （`indication = LocalIndication.current`），而不是自己写 ripple/缩放动画。
 *
 * 形态与 Material 版保持一致：极简、引文两行、右下角出处、内边距 16/14dp。
 */
@Composable
internal fun MiuixDailyQuoteCard(
    quote: DailyQuote,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshLabel = stringResource(Res.string.a11y_refresh_daily_quote)
    Surface(
        onClick = onRefresh,
        // Miuix 的 Surface 内部不透传 clickable 的 onClickLabel，这里补一个语义动作，
        // 让读屏能说明"点击可换一句"，同时不覆盖卡片自身的正文内容。
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                onClick(label = refreshLabel) {
                    onRefresh()
                    true
                }
            },
        color = MiuixTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = quote.text,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            quote.source?.let { source ->
                Text(
                    text = source,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 6.dp),
                )
            }
        }
    }
}
