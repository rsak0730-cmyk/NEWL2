package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidAppIconItem
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

private val RailLetters: List<String> =
    listOf("#") + ('A'..'Z').map { it.toString() }

private fun letterOf(label: String): String {
    val c = label.firstOrNull()?.uppercaseChar() ?: '#'
    return if (c in 'A'..'Z') c.toString() else "#"
}

@Composable
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
fun AppDrawerSheet(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val query = viewModel.searchQuery
    val drawerFont = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(com.manish.liquidlauncher.model.TypeTargets.DRAWER)
    }

    LaunchedEffect(viewModel.isDrawerOpen) {
        if (!viewModel.isDrawerOpen) {
            focusManager.clearFocus()
        }
    }

    val filteredApps = remember(viewModel.installedApps, query) {
        if (query.isBlank()) {
            viewModel.installedApps
        } else {
            viewModel.installedApps.filter {
                it.label.contains(query, ignoreCase = true)
            }
        }
    }

    val gridState = rememberLazyGridState()

    val firstIndexByLetter = remember(filteredApps) {
        val map = HashMap<String, Int>()

        filteredApps.forEachIndexed { i, app ->
            val letter = letterOf(app.label)
            if (!map.containsKey(letter)) {
                map[letter] = i
            }
        }

        map
    }

    var railLetter by remember {
        mutableStateOf<String?>(null)
    }

    var lastRailLetter by remember {
        mutableStateOf("A")
    }

    var pull by remember {
        mutableIntStateOf(0)
    }

    val nestedConnection = remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < 0f) {
                    pull = 0
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    pull += available.y.toInt()
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                if (pull > 160) {
                    viewModel.closeDrawer()
                }

                pull = 0
                return Velocity.Zero
            }
        }
    }

    AnimatedVisibility(
        visible = viewModel.isDrawerOpen,
        enter = fadeIn(tween(220)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                )
            ) { it / 2 } +
            scaleIn(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = 380f
                ),
                initialScale = 0.94f
            ),
        exit = fadeOut(tween(200)) +
            slideOutVertically(
                animationSpec = tween(240)
            ) { it / 3 } +
            scaleOut(
                animationSpec = tween(240),
                targetScale = 0.96f
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030712).copy(alpha = 0.70f),
                            Color(0xFF071324).copy(alpha = 0.88f)
                        )
                    )
                )
                .statusBarsPadding()
                .windowInsetsPadding(
                    WindowInsets.ime.union(
                        WindowInsets.navigationBars
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier.pointerInput(Unit) {

                        var total = 0f

                        detectVerticalDragGestures(
                            onDragStart = {
                                total = 0f
                            },
                            onDragEnd = {
                                if (total > 90f) {
                                    viewModel.closeDrawer()
                                }
                                total = 0f
                            },
                            onDragCancel = {
                                total = 0f
                            },
                            onVerticalDrag = { _, dy ->
                                total += dy
                            }
                        )
                    }
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                            .width(44.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(alpha = 0.35f)
                            )
                    )

                    LiquidGlassSurface(
                        config = config,
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 2.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = accent
                            )

                            TextField(
                                value = query,
                                onValueChange = {
                                    viewModel.searchQuery = it
                                },
                                placeholder = {
                                    Text(
                                        "Search ${viewModel.installedApps.size} apps...",
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {

                                        val first =
                                            filteredApps.firstOrNull()

                                        if (
                                            filteredApps.size == 1 &&
                                            first != null
                                        ) {
                                            viewModel.launchApp(
                                                context,
                                                view,
                                                first
                                            )
                                        } else if (filteredApps.isEmpty()) {
                                            viewModel.webSearch(
                                                context,
                                                query
                                            )
                                        } else {
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = accent
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (query.isNotEmpty()) {
                                        viewModel.searchQuery = ""
                                    } else {
                                        viewModel.closeDrawer()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close Drawer",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                if (query.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    GlassButton(
                        text = "Search the web for \"$query\"",
                        config = config,
                        icon = Icons.Rounded.Public,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.webSearch(context, query)
                    }
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                // ---- App drawer quick-actions -------------------------------------------
                if (query.isBlank()) {
                    SectionTitleDrawer("Quick actions", accent)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val recent = viewModel.recentApps.take(4)
                        if (recent.isNotEmpty()) {
                            GlassButton(
                                text = "Recent",
                                config = config,
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.AccessTime
                            ) { viewModel.openDrawer() }
                        }
                        GlassButton(
                            text = "Updated",
                            config = config,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Refresh
                        ) { viewModel.openDrawer() }
                        GlassButton(
                            text = "Frequent",
                            config = config,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Dashboard
                        ) { viewModel.openDrawer() }
                        GlassButton(
                            text = "Large",
                            config = config,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Cloud
                        ) { viewModel.openDrawer() }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(nestedConnection)
                ) {

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(
                            end = if (query.isBlank()) 22.dp else 0.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(
                            count = filteredApps.size,
                            key = {
                                filteredApps[it].packageName
                            }
                        ) { index ->

                            val app = filteredApps[index]

                            Box(
                                contentAlignment = Alignment.TopCenter
                            ) {

                                LiquidAppIconItem(
                                    app = app,
                                    config = config,
                                    fontFamily = drawerFont,
                                    entranceIndex =
                                        if (index < 24) index else -1,
                                    onLongClick = {
                                        viewModel.menuApp = app
                                    },
                                    onClick = {
                                        viewModel.launchApp(
                                            context,
                                            view,
                                            app
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (
                        filteredApps.isEmpty() &&
                        query.isNotBlank()
                    ) {
                        Text(
                            text = "No apps match \"$query\"",
                            color = Color.White.copy(alpha = 0.65f),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }

                    if (
                        query.isBlank() &&
                        filteredApps.size > 12
                    ) {

                        AlphabetRail(
                            available = firstIndexByLetter.keys,
                            accent = accent,
                            modifier = Modifier.align(
                                Alignment.CenterEnd
                            ),
                            onSelect = { letter ->

                                val start =
                                    RailLetters.indexOf(letter)

                                val target =
                                    RailLetters.drop(start)
                                        .firstOrNull {
                                            it in firstIndexByLetter
                                        }
                                    ?: RailLetters.lastOrNull {
                                        it in firstIndexByLetter
                                    }

                                val idx = target?.let {
                                    firstIndexByLetter[it]
                                }

                                if (idx != null) {
                                    scope.launch {
                                        gridState.scrollToItem(idx)
                                    }
                                }

                                if (config.haptics) {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove
                                    )
                                }

                                railLetter = letter
                                lastRailLetter = letter
                            },
                            onRelease = {
                                railLetter = null
                            }
                        )

                        if (railLetter != null) {

                            LiquidGlassSurface(
                                config = config,
                                cornerRadius = 36.dp,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 52.dp)
                                    .size(72.dp)
                            ) {

                                Text(
                                    text = lastRailLetter,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(
                                        Alignment.Center
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun AlphabetRail(
    available: Set<String>,
    accent: Color,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
    onRelease: () -> Unit
) {
    var active by remember {
        mutableIntStateOf(-1)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(22.dp)
            .padding(vertical = 6.dp)
            .pointerInput(Unit) {

                awaitEachGesture {

                    val down = awaitFirstDown()
                    val rows = RailLetters.size

                    fun pick(y: Float) {
                        val i = (
                            (y / size.height) * rows
                        ).toInt().coerceIn(0, rows - 1)

                        if (i != active) {
                            active = i
                            onSelect(RailLetters[i])
                        }
                    }

                    pick(down.position.y)
                    down.consume()

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()

                        if (change != null) {
                            pick(change.position.y)
                            change.consume()
                        }

                    } while (
                        event.changes.any { it.pressed }
                    )

                    active = -1
                    onRelease()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        RailLetters.forEachIndexed { i, letter ->

            val scale by animateFloatAsState(
                targetValue = if (i == active) 1.9f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.4f,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "railScale"
            )

            Text(
                text = letter,
                color = if (i == active) {
                    accent
                } else {
                    Color.White.copy(
                        alpha = if (letter in available) {
                            0.85f
                        } else {
                            0.28f
                        }
                    )
                },
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = -(scale - 1f) * 16f
                }
            )
        }
    }
}

@Composable
private fun SectionTitleDrawer(text: String, accent: Color) {
    Text(
        text = text.uppercase(),
        color = accent,
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(horizontal = 12.dp, top = 4.dp, bottom = 4.dp)
    )
}
