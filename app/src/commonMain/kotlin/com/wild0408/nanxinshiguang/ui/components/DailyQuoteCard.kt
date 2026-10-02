package com.wild0408.nanxinshiguang.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.model.DailyQuote
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_refresh_daily_quote
import org.jetbrains.compose.resources.stringResource

/**
 * 「每日一言」卡片（Material 3）。
 *
 * 极简形态：引文最多两行 + 右下角出处；整卡可点击刷新一句。
 * 容器色取低强调的 surfaceVariant，避免抢课程卡片的注意力。
 */
@Composable
fun DailyQuoteCard(
    quote: DailyQuote,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(Res.string.a11y_refresh_daily_quote),
                onClick = {
                    // 与 Miuix 版保持一致的点击触感（工程内可点击组件统一用 Confirm）
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onRefresh()
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            quote.source?.let { source ->
                Text(
                    text = source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 6.dp),
                )
            }
        }
    }
}
