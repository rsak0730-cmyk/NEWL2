package com.manish.liquidlauncher.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.features.FeatureStudioActivity
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.DockStyle
import com.manish.liquidlauncher.model.FolderState
import com.manish.liquidlauncher.model.GestureAction
import com.manish.liquidlauncher.model.GestureSlot
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.notifications.NotificationHub
import com.manish.liquidlauncher.ui.components.ActivityTimelineWidget
import com.manish.liquidlauncher.ui.components.AirQualityWidget
import com.manish.liquidlauncher.ui.components.CalendarWidget
import com.manish.liquidlauncher.ui.components.DeviceMonitorWidget
import com.manish.liquidlauncher.ui.components.EdgeGlow
import com.manish.liquidlauncher.ui.components.FloatingShortcutBubble
import com.manish.liquidlauncher.ui.components.GaugeCard
import com.manish.liquidlauncher.ui.components.GestureTrail
import com.manish.liquidlauncher.ui.components.GlassIconButton
import com.manish.liquidlauncher.ui.components.LiquidAppIconItem
import com.manish.liquidlauncher.ui.components.LiquidFolderItem
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.LiquidWallpaper
import com.manish.liquidlauncher.ui.components.LiveActivityIsland
import com.manish.liquidlauncher.ui.components.MusicWidget
import com.manish.liquidlauncher.ui.components.QuickTile
import com.manish.liquidlauncher.ui.components.SectionTitle
import com.manish.liquidlauncher.ui.components.SkyWidget
import com.manish.liquidlauncher.ui.components.TrailPoint
import com.manish.liquidlauncher.ui.components.WidgetStack
import com.manish.liquidlauncher.ui.components.WorldClockWidget
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.ui.components.rememberBatteryInfo
import com.manish.liquidlauncher.util.SoundFx
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private sealed interface HomeCell {
    val key: String

    data class FolderCell(val folder: FolderState) : HomeCell {
        override val key: String get() = "folder:" + folder.title
    }

    data class AppCell(val app: AppInfo) : HomeCell {
        override val key: String get() = "app:" + app.packageName
    }
}

private fun targetForKey(key: String): String =
    if (key.startsWith("folder:")) TypeTargets.folder(key.removePrefix("folder:"))
    else TypeTargets.app(key.removePrefix("app:"))

private val SocialKeys = listOf("whatsapp", "instagram", "telegram", "discord", "snapchat", "facebook", "twitter", "reddit", "signal")
private val GoogleKeys = listOf("google", "chrome", "youtube", "maps")
private val RowGap = 14.dp
private val WidgetBlockHeight = 134.dp

private data class UsageStat(val used: Long, val total: Long)

private fun UsageStatFraction(used: Long, total: Long): Float =
    if (total > 0) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

private fun readMemory(context: Context): UsageStat {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    return UsageStat(used = info.totalMem - info.availMem, total = info.totalMem)
}

private fun readStorage(): UsageStat {
    return try {
        val stat = android.os.StatFs(Environment.getDataDirectory().absolutePath)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        UsageStat(used = total - free, total = total)
    } catch (_: Exception) {
        UsageStat(0, 0)
    }
}

private fun gb(bytes: Long): String = String.format(java.util.Locale.US, "%.1f", bytes / 1_073_741_824.0)

private fun rowsThatFit(available: Dp, cell: Dp, gap: Dp): Int =
    (((available + gap) / (cell + gap)).toInt()).coerceIn(1, 7)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: LauncherViewModel, onPickWallpaper: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val battery by rememberBatteryInfo()
    var firstPage by remember { mutableStateOf(true) }
    val trail = remember { mutableStateListOf<TrailPoint>() }
    var totalPages by remember { mutableIntStateOf(2) }
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { totalPages })
    val columns = config.gridColumns.coerceIn(3, 5)

    val onLaunch: (AppInfo) -> Unit = { viewModel.launchApp(context, view, it) }
    val onMenu: (AppInfo) -> Unit = { viewModel.menuApp = it }

    // Fonts resolved once per change, not per frame
    val homeFont = remember(viewModel.typeRevision, viewModel.typeMap) { viewModel.typeFamily(TypeTargets.HOME) }
    val dockFont = remember(viewModel.typeRevision, viewModel.typeMap) { viewModel.typeFamily(TypeTargets.DOCK) }
    val clockFont = remember(viewModel.typeRevision, viewModel.typeMap) { viewModel.typeFamily(TypeTargets.CLOCK) }
    val batteryFont = remember(viewModel.typeRevision, viewModel.typeMap) { viewModel.typeFamily(TypeTargets.BATTERY) }

    LaunchedEffect(pagerState.currentPage) {
        if (firstPage) firstPage = false else SoundFx.play(SoundFx.SWISH)
    }

    LaunchedEffect(viewModel.homeTick) {
        if (viewModel.homeTick > 0) pagerState.animateScrollToPage(1)
    }

    // If items are removed while the user is on a later page, keep the pager in range
    LaunchedEffect(totalPages) {
        if (pagerState.currentPage > totalPages - 1) {
            pagerState.scrollToPage((totalPages - 1).coerceAtLeast(0))
        }
    }

    // Undo pill lifetime
    LaunchedEffect(viewModel.undoOrder) {
        if (viewModel.undoOrder != null) {
            delay(6000)
            viewModel.clearUndo()
        }
    }

    val customStates = remember(viewModel.customFolders, viewModel.installedApps) {
        val byPkg = viewModel.installedApps.associateBy { it.packageName }
        viewModel.customFolders.mapNotNull { cf ->
            val apps = cf.packages.mapNotNull { byPkg[it] }
            if (apps.isEmpty()) null else FolderState(cf.name, apps)
        }
    }
    val homeApps = remember(viewModel.installedApps, viewModel.dockApps, customStates) {
        val dock = viewModel.dockApps.map { it.packageName }.toSet()
        val custom = customStates.flatMap { f -> f.apps.map { it.packageName } }.toSet()
        viewModel.installedApps.filter { it.packageName !in dock && it.packageName !in custom }
    }
    val folders = remember(homeApps, customStates) {
        val social = homeApps.filter { a -> SocialKeys.any { a.packageName.contains(it) } }
        val socialSet = social.map { it.packageName }.toSet()
        val google = homeApps.filter { a ->
            a.packageName !in socialSet && GoogleKeys.any { a.packageName.contains(it) }
        }
        customStates + listOfNotNull(
            if (social.size >= 2) FolderState("Social", social) else null,
            if (google.size >= 2) FolderState("Google", google) else null
        )
    }
    val looseApps = remember(homeApps, folders) {
        val inFolders = folders.flatMap { f -> f.apps.map { it.packageName } }.toSet()
        homeApps.filter { it.packageName !in inFolders }
    }

    // One ordered list of home items; the persisted order wins, new items append.
    val cells: List<HomeCell> = remember(folders, looseApps, viewModel.workspaceOrder) {
        val all: List<HomeCell> = folders.map { HomeCell.FolderCell(it) } + looseApps.map { HomeCell.AppCell(it) }
        val byKey = all.associateBy { it.key }
        val stored = viewModel.workspaceOrder.mapNotNull { byKey[it] }
        val known = stored.map { it.key }.toSet()
        stored + all.filter { it.key !in known }
    }
    val fontForCell: (String) -> FontFamily = remember(cells, viewModel.typeRevision, viewModel.typeMap) {
        val map = cells.associate { it.key to viewModel.typeFamily(targetForKey(it.key)) }
        val fallback = homeFont
        val resolver: (String) -> FontFamily = { key -> map[key] ?: fallback }
        resolver
    }

    val gestureSwipeUp = viewModel.gestureMap.actionFor(GestureSlot.SwipeUp, GestureAction.Drawer)
    val gestureSwipeDown = viewModel.gestureMap.actionFor(GestureSlot.SwipeDown, GestureAction.Notifications)
    val gestureDoubleTap = viewModel.gestureMap.actionFor(GestureSlot.DoubleTap, GestureAction.Lock)
    val gestureLongPress = viewModel.gestureMap.actionFor(GestureSlot.LongPress, GestureAction.ThemeEngine)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(config.gestureTrail) {
                if (!config.gestureTrail) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        // Initial pass: we watch the finger without consuming, so taps,
                        // drags and pager swipes all behave exactly as before.
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.lastOrNull()
                        if (change != null && change.pressed) {
                            trail.add(TrailPoint(change.position, System.currentTimeMillis()))
                            if (trail.size > 26) trail.removeAt(0)
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gestureDoubleTap, gestureLongPress) {
                    detectTapGestures(
                        onDoubleTap = { viewModel.runGesture(gestureDoubleTap, context) },
                        onLongPress = { viewModel.runGesture(gestureLongPress, context) }
                    )
                }
                .pointerInput(gestureSwipeUp, gestureSwipeDown) {
                    var total = 0f
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f },
                        onDragEnd = {
                            if (total < -90f) viewModel.runGesture(gestureSwipeUp, context)
                            else if (total > 170f) viewModel.runGesture(gestureSwipeDown, context)
                            total = 0f
                        },
                        onDragCancel = { total = 0f },
                        onVerticalDrag = { _, dy -> total += dy }
                    )
                }
        ) {
            LiquidWallpaper(
                config = config,
                imageUri = viewModel.customWallpaperUri,
                pageOffset = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                modifier = Modifier.fillMaxSize()
            )
            if (config.gestureTrail) {
                GestureTrail(points = trail, accent = accent, modifier = Modifier.fillMaxSize())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 50.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .jellyTouch(haptics = config.haptics, pressX = 0.97f, pressY = 0.92f) {
                            viewModel.searchQuery = ""
                            viewModel.openDrawer()
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = accent,
                            modifier = Modifier.width(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Search ${viewModel.installedApps.size} apps",
                            color = Color.White.copy(alpha = 0.78f),
                            fontFamily = homeFont,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                GlassIconButton(
                    Icons.Rounded.TextFields,
                    "Type and language",
                    config,
                    size = 42.dp,
                    tint = accent
                ) { viewModel.openTypeStudio(TypeTargets.HOME) }
                GlassIconButton(Icons.Rounded.Wallpaper, "Import Wallpaper", config, size = 42.dp, onClick = onPickWallpaper)
                GlassIconButton(Icons.Rounded.Tune, "Theme Engine", config, size = 42.dp, tint = accent) {
                    viewModel.isThemeEngineOpen = true
                }
                GlassIconButton(Icons.Rounded.SwapHoriz, "Switch Default Launcher", config, size = 42.dp) {
                    viewModel.openDefaultLauncherPicker(context)
                }
                GlassIconButton(Icons.Rounded.Dashboard, "Feature Studio", config, size = 42.dp) {
                    context.startActivity(Intent(context, FeatureStudioActivity::class.java))
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = if (config.oneHanded) 84.dp else 0.dp, bottom = 8.dp)
            ) {
                val availableHeight = maxHeight
                val cellHeight = (60f * config.iconScale).dp + if (config.showLabels) 26.dp else 8.dp
                val rowsFull = rowsThatFit(availableHeight, cellHeight, RowGap).coerceAtMost(config.gridRows)
                val rowsFirst = rowsThatFit(availableHeight - WidgetBlockHeight - 16.dp, cellHeight, RowGap)
                    .coerceAtMost(config.gridRows)

                val firstCapacity = rowsFirst * columns
                val restCapacity = (rowsFull * columns).coerceAtLeast(1)
                val restCount = (cells.size - firstCapacity).coerceAtLeast(0)
                val extraPages = if (restCount == 0) 0 else (restCount + restCapacity - 1) / restCapacity
                val wantedPages = 2 + extraPages

                SideEffect {
                    if (totalPages != wantedPages) totalPages = wantedPages
                }

                val onMove: (Int, Int) -> Unit = { from, to ->
                    val keys = cells.map { it.key }.toMutableList()
                    if (from in keys.indices && to in keys.indices) {
                        val moved = keys.removeAt(from)
                        keys.add(to, moved)
                        viewModel.applyWorkspaceOrder(keys)
                    }
                }
                val onCreateFolder: (AppInfo, AppInfo) -> Unit = { source, target ->
                    val name = "${source.label.take(6)} & ${target.label.take(6)}"
                    viewModel.addToFolder(name, target)
                    viewModel.addToFolder(name, source)
                }
                val onEdgeScroll: (Int) -> Unit = { direction ->
                    scope.launch {
                        val target = (pagerState.currentPage + direction).coerceIn(0, totalPages - 1)
                        if (target != pagerState.currentPage) pagerState.animateScrollToPage(target)
                    }
                }

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    Box(modifier = Modifier.fillMaxSize().pageTransform(pagerState, page)) {
                        when (page) {
                            0 -> DashboardPage(viewModel, config, homeFont, onLaunch, onMenu)
                            1 -> Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().height(WidgetBlockHeight - 16.dp)) {
                                    WidgetStack(viewModel, config, Modifier.fillMaxSize())
                                }
                                CellGrid(
                                    cells = cells.take(firstCapacity),
                                    rows = rowsFirst,
                                    columns = columns,
                                    cellHeight = cellHeight,
                                    pageOffset = 0,
                                    config = config,
                                    animateIn = true,
                                    editMode = viewModel.editMode,
                                    fontFor = fontForCell,
                                    onFolder = { viewModel.openedFolder = it },
                                    onLaunch = onLaunch,
                                    onMenu = onMenu,
                                    onMove = onMove,
                                    onCreateFolder = onCreateFolder,
                                    onEdgeScroll = onEdgeScroll
                                )
                            }
                            else -> {
                                val start = firstCapacity + (page - 2) * restCapacity
                                CellGrid(
                                    cells = cells.drop(start).take(restCapacity),
                                    rows = rowsFull,
                                    columns = columns,
                                    cellHeight = cellHeight,
                                    pageOffset = start,
                                    config = config,
                                    animateIn = true,
                                    editMode = viewModel.editMode,
                                    fontFor = fontForCell,
                                    onFolder = { viewModel.openedFolder = it },
                                    onLaunch = onLaunch,
                                    onMenu = onMenu,
                                    onMove = onMove,
                                    onCreateFolder = onCreateFolder,
                                    onEdgeScroll = onEdgeScroll
                                )
                            }
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                // Glass page-indicator pill with a sliding selected chip.
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 14.dp,
                    sheen = false,
                    modifier = Modifier
                        .width((totalPages * 32 + 16).dp)
                        .height(26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(totalPages) { index ->
                            val selected = pagerState.currentPage == index
                            val isPressed by remember { mutableStateOf(false) }
                            val pressScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.85f else 1f,
                                animationSpec = spring(
                                    dampingRatio = 0.30f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "pageIndicatorPress"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) accent else Color.White.copy(alpha = 0.15f)
                                    )
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isPressed = true
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                    .graphicsLayer { scaleX = pressScale; scaleY = pressScale },
                                contentAlignment = Alignment.Center
                            )
                        }
                    }
                }

                DockBar(
                    viewModel = viewModel,
                    config = config,
                    fontFamily = dockFont,
                    onLaunch = onLaunch,
                    onMenu = onMenu
                )
            }
        }

        // Overlays that float above the workspace
        if (config.activityIsland) {
            Box(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 62.dp)) {
                LiveActivityIsland(viewModel, config)
            }
        }
        EdgeGlow(config = config, active = config.edgeLighting && battery.charging)
        if (config.floatingBubbles) {
            FloatingShortcutBubble(viewModel, config)
        }

        if (viewModel.editMode || viewModel.undoOrder != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 116.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (viewModel.editMode) {
                    EditChip("Edit mode", accent, Icons.Rounded.Edit, config) { viewModel.toggleEditMode() }
                }
                if (viewModel.undoOrder != null) {
                    EditChip("Undo move", accent, Icons.Rounded.Undo, config) { viewModel.undoWorkspaceMove() }
                }
            }
        }
    }
}

@Composable
private fun EditChip(
    text: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    config: LiquidThemeConfig,
    onClick: () -> Unit
) {
    LiquidGlassSurface(
        config = config,
        cornerRadius = 24.dp,
        modifier = Modifier.jellyTouch(haptics = true, pressX = 0.94f, pressY = 0.9f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.width(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.pageTransform(state: PagerState, page: Int): Modifier = graphicsLayer {
    val signed = (page - state.currentPage) - state.currentPageOffsetFraction
    val distance = abs(signed).coerceIn(0f, 1f)
    alpha = 1f - 0.65f * distance
    val s = 1f - 0.07f * distance
    scaleX = s
    scaleY = s
    rotationY = (signed * -18f).coerceIn(-18f, 18f)
    cameraDistance = 14f * density
    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(if (signed > 0f) 0f else 1f, 0.5f)
}

@Composable
private fun CellGrid(
    cells: List<HomeCell>,
    rows: Int,
    columns: Int,
    cellHeight: Dp,
    pageOffset: Int,
    config: LiquidThemeConfig,
    animateIn: Boolean,
    editMode: Boolean,
    fontFor: (String) -> FontFamily,
    onFolder: (FolderState) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onMenu: (AppInfo) -> Unit,
    onMove: (Int, Int) -> Unit,
    onCreateFolder: (AppInfo, AppInfo) -> Unit,
    onEdgeScroll: (Int) -> Unit
) {
    val density = LocalDensity.current
    var dragIndex by remember { mutableStateOf(-1) }
    var hoverIndex by remember { mutableStateOf(-1) }
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }
    val merged = remember { mutableStateOf(false) }
    var lastEdgeScroll by remember { mutableStateOf(0L) }

    // Hover over another app for a moment -> drag-to-create folder
    LaunchedEffect(dragIndex, hoverIndex, editMode) {
        if (editMode && dragIndex >= 0 && hoverIndex >= 0 && hoverIndex != dragIndex) {
            val source = cells.getOrNull(dragIndex)
            val target = cells.getOrNull(hoverIndex)
            if (source is HomeCell.AppCell && target is HomeCell.AppCell) {
                delay(750)
                merged.value = true
                onCreateFolder(source.app, target.app)
                dragIndex = -1
                hoverIndex = -1
                dragX = 0f
                dragY = 0f
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidthPx = with(density) { (maxWidth / columns).toPx() }.coerceAtLeast(1f)
        val cellHeightPx = with(density) { cellHeight.toPx() }
        val rowPitch = cellHeightPx + with(density) { RowGap.toPx() }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(RowGap)) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until columns) {
                        val index = r * columns + c
                        val cell = cells.getOrNull(index)
                        val isDragged = dragIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (cell != null) {
                                val dragModifier = if (editMode) {
                                    Modifier.pointerInput(cell.key, columns) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                merged.value = false
                                                dragIndex = index
                                                hoverIndex = index
                                                dragX = 0f
                                                dragY = 0f
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                dragX += amount.x
                                                dragY += amount.y
                                                val dCol = (dragX / cellWidthPx).roundToInt()
                                                val dRow = (dragY / rowPitch).roundToInt()
                                                hoverIndex = (index + dCol + dRow * columns)
                                                    .coerceIn(0, (cells.size - 1).coerceAtLeast(0))
                                                val now = System.currentTimeMillis()
                                                if (now - lastEdgeScroll > 900) {
                                                    if (dragX > cellWidthPx * (columns - 0.7f)) {
                                                        lastEdgeScroll = now
                                                        onEdgeScroll(1)
                                                    } else if (dragX < -cellWidthPx * (columns - 0.7f)) {
                                                        lastEdgeScroll = now
                                                        onEdgeScroll(-1)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                if (!merged.value && hoverIndex >= 0 && hoverIndex != index) {
                                                    onMove(pageOffset + index, pageOffset + hoverIndex)
                                                }
                                                dragIndex = -1
                                                hoverIndex = -1
                                                dragX = 0f
                                                dragY = 0f
                                            },
                                            onDragCancel = {
                                                dragIndex = -1
                                                hoverIndex = -1
                                                dragX = 0f
                                                dragY = 0f
                                            }
                                        )
                                    }
                                } else Modifier

                                Box(
                                    modifier = Modifier
                                        .then(dragModifier)
                                        .graphicsLayer {
                                            if (isDragged) {
                                                translationX = dragX
                                                translationY = dragY
                                                scaleX = 1.08f
                                                scaleY = 1.08f
                                                alpha = 0.9f
                                            }
                                        }
                                ) {
                                    when (cell) {
                                        is HomeCell.FolderCell -> LiquidFolderItem(
                                            title = cell.folder.title,
                                            apps = cell.folder.apps,
                                            config = config,
                                            fontFamily = fontFor(cell.key),
                                            onClick = { onFolder(cell.folder) }
                                        )
                                        is HomeCell.AppCell -> LiquidAppIconItem(
                                            app = cell.app,
                                            config = config,
                                            bob = config.iconBob && !editMode,
                                            entranceIndex = if (animateIn) index else -1,
                                            fontFamily = fontFor(cell.key),
                                            onLongClick = if (editMode) null else ({ onMenu(cell.app) }),
                                            onClick = { if (!editMode) onLaunch(cell.app) else onMenu(cell.app) }
                                        )
                                    }
                                    if (editMode) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .width(16.dp)
                                                .height(16.dp)
                                                .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockBar(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    fontFamily: FontFamily,
    onLaunch: (AppInfo) -> Unit,
    onMenu: (AppInfo) -> Unit
) {
    val apps = viewModel.dockApps
    val accent = config.accentColor()
    val iconSize = (56f * config.iconScale).dp

    when (config.dockStyle) {
        DockStyle.Invisible -> DockIcons(apps, config, fontFamily, iconSize, onLaunch, onMenu)
        DockStyle.Floating -> Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.forEach { app ->
                LiquidGlassSurface(config = config, cornerRadius = 22.dp, modifier = Modifier.size(64.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LiquidAppIconItem(
                            app = app,
                            config = config,
                            showLabel = false,
                            iconBoxSize = 44.dp,
                            bob = false,
                            entranceIndex = -1,
                            fontFamily = fontFamily,
                            onLongClick = { onMenu(app) },
                            onClick = { onLaunch(app) }
                        )
                    }
                }
            }
        }
        DockStyle.Minimal -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.07f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.forEach { app ->
                LiquidAppIconItem(
                    app = app,
                    config = config,
                    showLabel = false,
                    iconBoxSize = iconSize,
                    bob = false,
                    entranceIndex = -1,
                    fontFamily = fontFamily,
                    onLongClick = { onMenu(app) },
                    onClick = { onLaunch(app) }
                )
            }
        }
        DockStyle.Frosted -> LiquidGlassSurface(
            config = config,
            cornerRadius = 10.dp,
            sheen = false,
            tintOverride = accent.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) { DockIcons(apps, config, fontFamily, iconSize, onLaunch, onMenu) }
        DockStyle.GlassPill -> LiquidGlassSurface(
            config = config,
            cornerRadius = 32.dp,
            modifier = Modifier.fillMaxWidth()
        ) { DockIcons(apps, config, fontFamily, iconSize, onLaunch, onMenu) }
    }
}

@Composable
private fun DockIcons(
    apps: List<AppInfo>,
    config: LiquidThemeConfig,
    fontFamily: FontFamily,
    iconSize: Dp,
    onLaunch: (AppInfo) -> Unit,
    onMenu: (AppInfo) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            LiquidAppIconItem(
                app = app,
                config = config,
                showLabel = false,
                iconBoxSize = iconSize,
                bob = false,
                entranceIndex = -1,
                fontFamily = fontFamily,
                onLongClick = { onMenu(app) },
                onClick = { onLaunch(app) }
            )
        }
    }
}

@Composable
private fun DashboardPage(
    viewModel: LauncherViewModel,
    config: LiquidThemeConfig,
    fontFamily: FontFamily,
    onLaunch: (AppInfo) -> Unit,
    onMenu: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    val accent = config.accentColor()
    val alertsOn by NotificationHub.connected
    var memory by remember { mutableStateOf(readMemory(context)) }
    var storage by remember { mutableStateOf(readStorage()) }

    LaunchedEffect(Unit) {
        while (true) {
            memory = readMemory(context)
            storage = readStorage()
            delay(4000)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Control Center",
            color = Color.White,
            fontFamily = fontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        MusicWidget(
            config = config,
            onEnableAccess = { viewModel.openSettingsScreen(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) },
            modifier = Modifier.fillMaxWidth().height(96.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().height(168.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GaugeCard(
                title = "MEMORY",
                detail = "${gb(memory.used)} / ${gb(memory.total)} GB",
                fraction = UsageStatFraction(memory.used, memory.total),
                config = config,
                modifier = Modifier.weight(1f)
            )
            GaugeCard(
                title = "STORAGE",
                detail = "${gb(storage.used)} / ${gb(storage.total)} GB",
                fraction = UsageStatFraction(storage.used, storage.total),
                config = config,
                modifier = Modifier.weight(1f)
            )
        }

        SkyWidget(config = config, modifier = Modifier.fillMaxWidth().height(110.dp))

        SectionTitle("Quick tiles", accent)
        Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile(
                icon = if (viewModel.torchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                label = "Torch",
                active = viewModel.torchOn,
                config = config,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) { viewModel.toggleTorch() }
            QuickTile(Icons.Rounded.Wifi, "Wi-Fi", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openSettingsScreen(context, Settings.ACTION_WIFI_SETTINGS)
            }
            QuickTile(Icons.Rounded.Bluetooth, "Bluetooth", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openSettingsScreen(context, Settings.ACTION_BLUETOOTH_SETTINGS)
            }
            QuickTile(Icons.Rounded.Settings, "Settings", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openSettingsScreen(context, Settings.ACTION_SETTINGS)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile(Icons.Rounded.NightsStay, "Focus", viewModel.focusMode, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.toggleFocus()
            }
            QuickTile(Icons.Rounded.AccessTime, "Idle", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.idleOpen = true
            }
            QuickTile(Icons.Rounded.Lock, "Lock", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.lockScreen(context)
            }
            QuickTile(Icons.Rounded.Notifications, "Alerts", alertsOn, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openSettingsScreen(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile(Icons.Rounded.Edit, "Edit", viewModel.editMode, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.toggleEditMode()
            }
            QuickTile(Icons.Rounded.TextFields, "Type", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openTypeStudio(TypeTargets.HOME)
            }
            QuickTile(Icons.Rounded.VisibilityOff, "Private", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.openPrivateSpace()
            }
            QuickTile(Icons.Rounded.Tune, "Theme", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.isThemeEngineOpen = true
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile(Icons.Rounded.Folder, "Files", false, config, Modifier.weight(1f).fillMaxHeight()) {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                } catch (_: Exception) {
                    viewModel.openSettingsScreen(context, Settings.ACTION_SETTINGS)
                }
            }
            QuickTile(Icons.Rounded.Gesture, "Gestures", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.gestureStudioOpen = true
            }
            QuickTile(Icons.Rounded.PushPin, "Carousel", false, config, Modifier.weight(1f).fillMaxHeight()) {
                viewModel.carouselOpen = true
            }
            QuickTile(Icons.Rounded.Dashboard, "Studio", false, config, Modifier.weight(1f).fillMaxHeight()) {
                context.startActivity(Intent(context, FeatureStudioActivity::class.java))
            }
        }

        SectionTitle("Widgets", accent)
        Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeatherCard(viewModel, config, Modifier.weight(1f).fillMaxHeight())
            DeviceMonitorWidget(config, Modifier.weight(1f).fillMaxHeight(), fontFamily)
            if (config.calendarWidgetEnabled) {
                CalendarWidget(config, Modifier.weight(1f).fillMaxHeight(), fontFamily)
            }
            if (config.airQualityWidgetEnabled) {
                AirQualityWidget(config, Modifier.weight(1f).fillMaxHeight(), fontFamily)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WorldClockWidget(config, Modifier.weight(1f).fillMaxHeight(), fontFamily)
            ActivityTimelineWidget(viewModel, config, Modifier.weight(1f).fillMaxHeight(), fontFamily)
        }

        if (viewModel.appPairs.isNotEmpty()) {
            SectionTitle("App pairs", accent)
            Row(modifier = Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (pair in viewModel.appPairs.take(4)) {
                    val firstApp = viewModel.installedApps.firstOrNull { it.packageName == pair.first }
                    val secondApp = viewModel.installedApps.firstOrNull { it.packageName == pair.second }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        LiquidGlassSurface(
                            config = config,
                            cornerRadius = 20.dp,
                            modifier = Modifier.fillMaxSize()
                                .jellyTouch(haptics = config.haptics, pressX = 0.92f, pressY = 0.9f, onClick = {
                                    viewModel.launchPair(context, pair)
                                })
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (firstApp != null && secondApp != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Image(
                                            bitmap = firstApp.icon,
                                            contentDescription = firstApp.label,
                                            modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Image(
                                            bitmap = secondApp.icon,
                                            contentDescription = secondApp.label,
                                            modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                        )
                                    }
                                }
                                Text(
                                    text = pair.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        AppsRow("Suggested right now", viewModel.suggestedApps, "Open apps for a few days and the ones you use at this time of day appear here.", config, fontFamily, onLaunch, onMenu)
        AppsRow("Frequently used", viewModel.frequentApps, "Apps you open often will float up here.", config, fontFamily, onLaunch, onMenu)
    }
}

@Composable
private fun WeatherCard(viewModel: LauncherViewModel, config: LiquidThemeConfig, modifier: Modifier) {
    val font = remember(viewModel.typeRevision, viewModel.typeMap) { viewModel.typeFamily(TypeTargets.WEATHER) }
    com.manish.liquidlauncher.ui.components.WeatherWidget(viewModel, config, modifier, font)
}

@Composable
private fun AppsRow(
    title: String,
    apps: List<AppInfo>,
    emptyText: String,
    config: LiquidThemeConfig,
    fontFamily: FontFamily,
    onLaunch: (AppInfo) -> Unit,
    onMenu: (AppInfo) -> Unit
) {
    SectionTitle(title, config.accentColor())
    if (apps.isEmpty()) {
        Text(
            text = emptyText,
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = fontFamily,
            fontSize = 12.sp
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 0 until 4) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    val app = apps.getOrNull(i)
                    if (app != null) {
                        LiquidAppIconItem(
                            app = app,
                            config = config,
                            bob = config.iconBob,
                            fontFamily = fontFamily,
                            onLongClick = { onMenu(app) },
                            onClick = { onLaunch(app) }
                        )
                    }
                }
            }
        }
    }
}
