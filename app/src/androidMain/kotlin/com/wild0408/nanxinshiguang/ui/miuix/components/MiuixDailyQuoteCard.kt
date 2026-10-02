package com.wild0408.nanxinshiguang.ui.miuix.components

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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 「每日一言」卡片（Miuix）。
 *
 * 用 Miuix 的 `Card` 承载：圆角取 `CardDefaults.CornerRadius`（16dp），与工程内其它 Miuix 卡片
 * 完全一致——此前误用 `Surface`，它的默认形状与卡片圆角不同，导致圆角看起来不对。
 *
 * 按压反馈用 `pressFeedbackType = PressFeedbackType.Sink`（官方下沉效果）；该参数在 `Card` 上
 * 默认是 `None`，不显式指定就没有任何反馈。
 *
 * 形态与 Material 版一致：极简、引文最多两行、右下角出处、内边距 16/14dp。
 */
@Composable
internal fun MiuixDailyQuoteCard(
    quote: DailyQuote,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshLabel = stringResource(Res.string.a11y_refresh_daily_quote)
    Card(
        onClick = onRefresh,
        pressFeedbackType = PressFeedbackType.Sink,
        // Card 内部不透传 clickable 的 onClickLabel，补一个语义动作让读屏能说明"点击可换一句"，
        // 同时不覆盖卡片自身的正文内容。
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                onClick(label = refreshLabel) {
                    onRefresh()
                    true
                }
            },
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Text(
            text = quote.text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
        )
        quote.source?.let { source ->
            Text(
                text = source,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
            )
        }
    }
}
