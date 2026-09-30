package com.wild0408.nanxinshiguang.ui.miuix.schoolselection

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.CategoryLastSchool
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.schoolselection.list.SchoolSelectionViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import school_index.Adapter
import school_index.AdapterCategory
import school_index.School
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_delete
import nanxinshiguang.generated.resources.category_bachelor_associate
import nanxinshiguang.generated.resources.category_general_tool
import nanxinshiguang.generated.resources.category_other
import nanxinshiguang.generated.resources.category_postgraduate
import nanxinshiguang.generated.resources.close_24px
import nanxinshiguang.generated.resources.info_24px
import nanxinshiguang.generated.resources.label_contributor_format
import nanxinshiguang.generated.resources.label_contributor_unknown
import nanxinshiguang.generated.resources.label_recent_visit
import nanxinshiguang.generated.resources.search_hint_school
import nanxinshiguang.generated.resources.school_24px
import nanxinshiguang.generated.resources.text_no_adapter_for_category_school
import nanxinshiguang.generated.resources.text_no_detailed_description
import nanxinshiguang.generated.resources.text_no_school_found
import nanxinshiguang.generated.resources.title_select_school
import nanxinshiguang.generated.resources.title_update_repo_screen
import nanxinshiguang.generated.resources.update_24px
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixSchoolSelectionListScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: SchoolSelectionViewModel = koinViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredSchools by viewModel.filteredSchools.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val schoolHistory by viewModel.schoolHistory.collectAsState()
    var query by rememberSaveable { mutableStateOf(searchQuery) }

    LaunchedEffect(query) { viewModel.updateSearchQuery(query) }

    val title = stringResource(Res.string.title_select_school)
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val categories = listOf(
        stringResource(Res.string.category_bachelor_associate),
        stringResource(Res.string.category_postgraduate),
        stringResource(Res.string.category_general_tool),
    )
    val categoryIndex = viewModel.displayCategories.indexOf(selectedCategory).coerceAtLeast(0)
    val listState = rememberLazyListState()
    val recent = recentSchool(schoolHistory, selectedCategory)
    val groupedSchools = remember(filteredSchools) {
        filteredSchools
            .groupBy { schoolInitial(it) }
            .toSortedMap()
    }
    val indexPositions = remember(groupedSchools, recent) {
        buildMap {
            var position = if (recent != null) 1 else 0
            groupedSchools.forEach { (initial, schools) ->
                this[initial] = position
                position += 1 + schools.size
            }
        }
    }
    val currentInitial by remember(groupedSchools, indexPositions) {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            indexPositions.entries
                .filter { it.value <= firstVisibleIndex }
                .maxByOrNull { it.value }
                ?.key
                ?: groupedSchools.keys.firstOrNull()
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                scrollBehavior.nativeState.contentOffset = if (index > 0 || offset > 0) -1f else 0f
            }
    }
    LaunchedEffect(selectedCategory, query) {
        listState.scrollToItem(0)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                largeTitle = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s ->
                    HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s)
                },
                endAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = { onNavigate(Destination.UpdateRepo) },
                        backdrop = backdrop,
                        icon = vectorResource(Res.drawable.update_24px),
                        contentDescription = stringResource(Res.string.title_update_repo_screen),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
                supplementaryContent = { topBarBottom ->
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = topBarBottom)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MiuixSchoolSearchField(query, { query = it })
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            categories.forEachIndexed { index, category ->
                                val selected = index == categoryIndex
                                Button(
                                    onClick = { viewModel.updateSelectedCategory(viewModel.displayCategories[index]) },
                                    modifier = Modifier.weight(1f),
                                    minHeight = 40.dp,
                                    insideMargin = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (selected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface,
                                    ),
                                ) { Text(category) }
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        val listContentPadding = PaddingValues(
            start = padding.calculateLeftPadding(direction) + 20.dp,
            top = padding.calculateTopPadding() + 12.dp,
            end = padding.calculateRightPadding(direction) + 20.dp,
            bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
        )
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = listContentPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                        if (isLoading) {
                            item { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        } else if (filteredSchools.isEmpty()) {
                            item { Text(stringResource(Res.string.text_no_school_found), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
                        } else {
                            recent?.let { recentSchool ->
                                item {
                                    MiuixRecentSchoolCard(
                                        school = recentSchool,
                                        onClick = { onNavigate(toAdapterSelection(recentSchool.toSchool(), selectedCategory)) },
                                        onDelete = { viewModel.clearHistory(selectedCategory) },
                                    )
                                }
                            }
                            groupedSchools.forEach { (initial, schools) ->
                                stickyHeader { MiuixAlphabetHeader(initial) }
                                items(schools, key = { it.id }) { school ->
                                    MiuixSchoolCard(school) { onNavigate(toAdapterSelection(school, selectedCategory)) }
                                }
                            }
                        }
                    }
                if (groupedSchools.isNotEmpty() && !isLoading) {
                    MiuixAlphabetIndexer(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            // Keep the index inside the Scaffold content area; the top bar also
                            // contains the fixed search and category controls.
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding(),
                            ),
                        initials = groupedSchools.keys.toList(),
                        indexPositions = indexPositions,
                        listState = listState,
                        selectedInitial = currentInitial,
                        onIndexSelected = {
                            scrollBehavior.nativeState.heightOffset = scrollBehavior.nativeState.heightOffsetLimit
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixSchoolSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        expanded = false,
        onExpandedChange = {},
        content = {},
        inputField = {
            InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                label = stringResource(Res.string.search_hint_school),
            )
        },
    )
}

@Composable
private fun MiuixAlphabetHeader(initial: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = initial,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MiuixAlphabetIndexer(
    modifier: Modifier = Modifier,
    initials: List<String>,
    indexPositions: Map<String, Int>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedInitial: String?,
    onIndexSelected: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val alphabet = remember(initials) { ('A'..'Z').map(Char::toString) + initials.filter { it.length != 1 || it[0] !in 'A'..'Z' } }
    val available = remember(initials) { initials.toSet() }
    var draggedInitial by remember { mutableStateOf<String?>(null) }
    var barHeight by remember { mutableIntStateOf(0) }
    fun selectAt(y: Float) {
        if (barHeight <= 0) return
        val selectedIndex = (y / barHeight * alphabet.size).toInt().coerceIn(0, alphabet.lastIndex)
        val initial = alphabet[selectedIndex]
        if (initial == draggedInitial) return
        indexPositions[initial]?.let { position ->
            draggedInitial = initial
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onIndexSelected()
            scope.launch { listState.scrollToItem(position) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(30.dp)
            .padding(top = 12.dp, bottom = 24.dp, end = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .onGloballyPositioned { barHeight = it.size.height }
                .pointerInput(alphabet, indexPositions) {
                    detectTapGestures { offset -> selectAt(offset.y); draggedInitial = null }
                }
                .pointerInput(alphabet, indexPositions) {
                    detectVerticalDragGestures(
                        onDragStart = { selectAt(it.y) },
                        onVerticalDrag = { change, _ -> change.consume(); selectAt(change.position.y) },
                        onDragEnd = { draggedInitial = null },
                        onDragCancel = { draggedInitial = null },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            alphabet.forEach { initial ->
                val isAvailable = initial in available
                val isSelected = initial == (draggedInitial ?: selectedInitial) && isAvailable
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        style = MiuixTheme.textStyles.footnote1,
                        color = when {
                            isSelected -> MiuixTheme.colorScheme.primary
                            isAvailable -> MiuixTheme.colorScheme.onSurfaceVariantActions
                            else -> MiuixTheme.colorScheme.outline
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

private fun schoolInitial(school: School): String = school.initial.trim().uppercase().firstOrNull()?.toString() ?: "#"

@Composable
internal fun MiuixAdapterSelectionScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    schoolId: String,
    schoolName: String,
    categoryNumber: Int,
    resourceFolder: String,
    viewModel: SchoolSelectionViewModel = koinViewModel(),
) {
    var adapters by remember(schoolId, categoryNumber) { mutableStateOf<List<Adapter>>(emptyList()) }
    var loading by remember(schoolId, categoryNumber) { mutableStateOf(true) }
    val category = remember(categoryNumber) { AdapterCategory.fromValue(categoryNumber) ?: AdapterCategory.BACHELOR_AND_ASSOCIATE }
    val categoryName = categoryName(category)
    val title = "$schoolName - $categoryName"
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current

    LaunchedEffect(schoolId, category) {
        loading = true
        viewModel.updateSelectedCategory(category)
        adapters = runCatching { viewModel.getAdaptersForSchoolAndCategory(schoolId) }.getOrDefault(emptyList())
        loading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                largeTitle = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = padding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 20.dp,
                    bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    loading -> item { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    adapters.isEmpty() -> item { Text(stringResource(Res.string.text_no_adapter_for_category_school, categoryName), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
                    else -> items(adapters, key = { it.adapter_id }) { adapter ->
                        MiuixAdapterCard(adapter) {
                            val initialUrl = adapter.import_url.orEmpty().ifBlank { "about:blank" }
                            val jsPath = adapter.asset_js_path.orEmpty().ifBlank { "${adapter.adapter_id}.js" }
                            onNavigate(Destination.WebView(initialUrl = initialUrl, assetJsPath = "$resourceFolder/$jsPath"))
                        }
                    }
                }
            }
        }
    }
}

private fun toAdapterSelection(school: School, category: AdapterCategory) = Destination.AdapterSelection(
    schoolId = school.id,
    schoolName = school.name,
    categoryNumber = category.value,
    resourceFolder = school.resource_folder,
)

private fun recentSchool(history: com.wild0408.nanxinshiguang.data.model.SchoolHistoryModel, category: AdapterCategory): CategoryLastSchool? {
    val value = when (category) {
        AdapterCategory.BACHELOR_AND_ASSOCIATE -> history.bachelor
        AdapterCategory.POSTGRADUATE -> history.postgraduate
        AdapterCategory.GENERAL_TOOL -> history.general
        else -> CategoryLastSchool()
    }
    return value.takeUnless { it.isEmpty }
}

@Composable
private fun categoryName(category: AdapterCategory): String = when (category) {
    AdapterCategory.BACHELOR_AND_ASSOCIATE -> stringResource(Res.string.category_bachelor_associate)
    AdapterCategory.POSTGRADUATE -> stringResource(Res.string.category_postgraduate)
    AdapterCategory.GENERAL_TOOL -> stringResource(Res.string.category_general_tool)
    else -> stringResource(Res.string.category_other)
}

@Composable
private fun MiuixSchoolCard(school: School, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(end = 44.dp),
        onClick = onClick,
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(vectorResource(Res.drawable.school_24px), null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(school.name, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MiuixRecentSchoolCard(school: CategoryLastSchool, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(end = 44.dp),
        onClick = onClick,
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.label_recent_visit), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.height(4.dp))
                Text(school.name, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDelete) { Icon(vectorResource(Res.drawable.close_24px), stringResource(Res.string.a11y_delete)) }
        }
    }
}

@Composable
private fun MiuixAdapterCard(adapter: Adapter, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(adapter.adapter_name, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(adapter.description.ifBlank { stringResource(Res.string.text_no_detailed_description) }, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(vectorResource(Res.drawable.info_24px), null, tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(16.dp))
                Text(stringResource(Res.string.label_contributor_format, adapter.maintainer.ifBlank { stringResource(Res.string.label_contributor_unknown) }), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }
    }
}
