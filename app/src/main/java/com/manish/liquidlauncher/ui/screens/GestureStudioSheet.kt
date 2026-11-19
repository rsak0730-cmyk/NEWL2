package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.GestureAction
import com.manish.liquidlauncher.model.GestureSlot
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

/**
 * Gesture Studio + App Pairs.
 *
 * Gestures: what each launcher gesture does (swipe up/down, double tap, long press).
 * App Pairs: two apps launched together in split screen.
 */
@Composable
fun GestureStudioSheet(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val context = LocalContext.current
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
    var picking by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf(listOf<String>()) }
    var pairName by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = viewModel.gestureStudioOpen,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.66f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.gestureStudioOpen = false }
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.popIn()) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gesture Studio",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = { viewModel.resetGestures() }) {
                                    Icon(Icons.Rounded.Refresh, "Reset gestures", tint = accent)
                                }
                                IconButton(onClick = { viewModel.gestureStudioOpen = false }) {
                                    Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                                }
                            }
                        }

                        GestureSlot.values().forEach { slot ->
                            val current = viewModel.gestureMap.assignments[slot]
                            Text(
                                text = slot.label,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GestureAction.values().forEach { action ->
                                    val selected = current == action
                                    Box(
                                        modifier = Modifier
                                            .jellyTouch(haptics = true, pressX = 0.94f, pressY = 0.9f) {
                                                viewModel.setGesture(slot, action)
                                            }
                                            .background(
                                                if (selected) accent.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.08f),
                                                CircleShape
                                            )
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = action.label,
                                            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "APP PAIRS",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        if (viewModel.appPairs.isEmpty()) {
                            Text(
                                text = "Pair two apps to open them side by side.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        viewModel.appPairs.forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pair.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Open",
                                    color = accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.launchPair(context, pair) }
                                        .padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.removePair(pair.name) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        "Remove pair",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (!picking) {
                            GlassButton("New app pair", config, Modifier.fillMaxWidth()) {
                                picked = emptyList()
                                pairName = ""
                                picking = true
                            }
                        } else {
                            Text(
                                text = "Pick two apps (${picked.size}/2)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                items(viewModel.installedApps, key = { it.packageName }) { app ->
                                    PickerApp(
                                        app = app,
                                        selected = app.packageName in picked,
                                        onClick = {
                                            picked = if (app.packageName in picked) {
                                                picked - app.packageName
                                            } else {
                                                (picked + app.packageName).take(2)
                                            }
                                        }
                                    )
                                }
                            }
                            LiquidGlassSurface(
                                config = config,
                                cornerRadius = 16.dp,
                                sheen = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextField(
                                    value = pairName,
                                    onValueChange = { pairName = it.take(18) },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            "Pair name",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            GlassButton("Save pair", config, Modifier.fillMaxWidth()) {
                                if (picked.size == 2) {
                                    viewModel.addPair(pairName, picked[0], picked[1])
                                    picking = false
                                    picked = emptyList()
                                    pairName = ""
                                }
                            }
                            GlassButton("Cancel", config, Modifier.fillMaxWidth()) {
                                picking = false
                                picked = emptyList()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerApp(app: AppInfo, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.jellyTouch(haptics = true, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Color.White.copy(alpha = 0.35f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
            )
        }
        Text(
            text = app.label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(58.dp)
        )
    }
}
