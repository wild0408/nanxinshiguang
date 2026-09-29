package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.desc_electricity
import nanxinshiguang.generated.resources.desc_grade_center
import nanxinshiguang.generated.resources.electricity_24px
import nanxinshiguang.generated.resources.item_electricity
import nanxinshiguang.generated.resources.item_grade_center
import nanxinshiguang.generated.resources.list_alt_24px
import nanxinshiguang.generated.resources.nav_service
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Immutable
private data class ServiceCardItem(
    val title: String,
    val description: String,
    val icon: DrawableResource,
    val destination: Destination,
)

@Composable
internal fun MiuixServiceScreen(onNavigate: (Destination) -> Unit) {
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    val serviceItems = listOf(
        ServiceCardItem(
            title = stringResource(Res.string.item_grade_center),
            description = stringResource(Res.string.desc_grade_center),
            icon = Res.drawable.list_alt_24px,
            destination = Destination.GradeCenter,
        ),
        ServiceCardItem(
            title = stringResource(Res.string.item_electricity),
            description = stringResource(Res.string.desc_electricity),
            icon = Res.drawable.electricity_24px,
            destination = Destination.ElectricityCenter,
        ),
    )

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = background,
            topBar = {
                HyperGlassTopBar(
                    title = stringResource(Res.string.nav_service),
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = padding.calculateLeftPadding(direction) + 20.dp,
                        top = padding.calculateTopPadding() + 12.dp,
                        end = padding.calculateRightPadding(direction) + 20.dp,
                        bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(serviceItems) { item ->
                        MiuixServiceItem(
                            item = item,
                            onOpen = { onNavigate(item.destination) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixServiceItem(
    item: ServiceCardItem,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Sink,
        onClick = onOpen,
    ) {
        Row(
            // The card's 92dp minimum height includes 14dp vertical insets on
            // both sides. Give the content the remaining space explicitly so
            // the icon, text and trailing action are centered as a group.
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ServiceIcon(item.icon)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 8.dp),
            ) {
                Text(
                    text = item.title,
                    style = MiuixTheme.textStyles.title3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.description,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(
                onClick = onOpen,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.chevron_right_24px),
                    contentDescription = "打开${item.title}",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                )
            }
        }
    }
}

@Composable
private fun ServiceIcon(icon: DrawableResource) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MiuixTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = vectorResource(icon),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MiuixTheme.colorScheme.onPrimaryContainer,
        )
    }
}
