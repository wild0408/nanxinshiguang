package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.text_loading_failed
import nanxinshiguang.generated.resources.title_open_source_licenses
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class MiuixLibraryInfo(
    val id: String,
    val name: String,
    val version: String?,
    val license: String?,
    val website: String?,
)

private sealed interface MiuixLibraryState {
    data object Loading : MiuixLibraryState
    data class Success(val libraries: List<MiuixLibraryInfo>) : MiuixLibraryState
    data class Error(val message: String) : MiuixLibraryState
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun MiuixOpenSourceLicensesScreen(onBack: () -> Unit) {
    val state by produceState<MiuixLibraryState>(MiuixLibraryState.Loading) {
        value = runCatching {
            val root = Json.parseToJsonElement(Res.readBytes("files/aboutlibraries.json").decodeToString()).jsonObject
            val libraries = root["libraries"]?.jsonArray.orEmpty().mapNotNull { element ->
                val item = element.jsonObject
                val id = item["uniqueId"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                MiuixLibraryInfo(
                    id = id,
                    name = item["name"]?.jsonPrimitive?.contentOrNull ?: id,
                    version = item["artifactVersion"]?.jsonPrimitive?.contentOrNull,
                    license = item["licenses"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.contentOrNull,
                    website = item["website"]?.jsonPrimitive?.contentOrNull
                        ?: item["scm"]?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull,
                )
            }.sortedBy { it.name.lowercase() }
            MiuixLibraryState.Success(libraries)
        }.getOrElse { MiuixLibraryState.Error(it.message ?: it.toString()) }
    }
    val uriHandler = LocalUriHandler.current
    val title = stringResource(Res.string.title_open_source_licenses)
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                largeTitle = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { backdropAlpha, shadowAlpha ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = backdropAlpha,
                        shadowAlpha = shadowAlpha,
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        val contentPadding = PaddingValues(
            start = scaffoldPadding.calculateLeftPadding(layoutDirection) + 20.dp,
            top = scaffoldPadding.calculateTopPadding() + 12.dp,
            end = scaffoldPadding.calculateRightPadding(layoutDirection) + 20.dp,
            bottom = scaffoldPadding.calculateBottomPadding() +
                hostPadding.calculateBottomPadding() + 20.dp,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .layerBackdrop(backdrop),
        ) {
        when (val value = state) {
            MiuixLibraryState.Loading -> Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is MiuixLibraryState.Error -> Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.text_loading_failed, value.message), color = MiuixTheme.colorScheme.error)
            }
            is MiuixLibraryState.Success -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(value.libraries, key = MiuixLibraryInfo::id) { library ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { library.website?.let(uriHandler::openUri) },
                        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(library.name, modifier = Modifier.weight(1f), style = MiuixTheme.textStyles.body1, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                library.version?.let { Text(it, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.footnote1) }
                            }
                            Text(
                                library.license ?: library.id,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                style = MiuixTheme.textStyles.footnote1,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
