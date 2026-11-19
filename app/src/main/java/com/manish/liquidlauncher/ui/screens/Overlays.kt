package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.FolderState
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidAppIconItem
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

/** Pop-up folder with a bouncy open/close. */
@Composable
fun FolderOverlay(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val context = LocalContext.current
    val view = LocalView.current
    val folder = viewModel.openedFolder
    var last by remember { mutableStateOf<FolderState?>(null) }
    SideEffect { if (folder != null) last = folder }
    val shown = folder ?: last

    AnimatedVisibility(
        visible = folder != null,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF02060D).copy(alpha = 0.78f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.openedFolder = null }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.popIn()) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 32.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val folderFont = remember(viewModel.typeRevision, viewModel.typeMap, shown?.title) {
                            viewModel.typeFamily(TypeTargets.folder(shown?.title ?: ""))
                        }
                        Text(
                            text = shown?.title ?: "",
                            color = Color.White,
                            fontFamily = folderFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val isCustom = viewModel.customFolders.any { it.name == shown?.title }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(shown?.apps ?: emptyList<AppInfo>(), key = { it.packageName }) { app ->
                                Box(contentAlignment = Alignment.TopCenter) {
                                    LiquidAppIconItem(
                                        app = app,
                                        config = config,
                                        entranceIndex = 0,
                                        fontFamily = folderFont,
                                        onLongClick = { viewModel.menuApp = app },
                                        onClick = { viewModel.launchApp(context, view, app) }
                                    )
                                }
                            }
                        }
                        GlassButton(
                            "Text & language", config, Modifier.fillMaxWidth(), Icons.Rounded.TextFields
                        ) {
                            viewModel.openedFolder = null
                            viewModel.openTypeStudio(TypeTargets.folder(shown?.title ?: ""))
                        }
                        if (isCustom) {
                            GlassButton(
                                "Delete folder", config, Modifier.fillMaxWidth(), Icons.Rounded.Delete,
                                tint = Color(0xFFFF6B6B)
                            ) { viewModel.deleteFolder(shown?.title ?: "") }
                        }
                    }
                }
            }
        }
    }
}

/** Long-press menu for any app: open, info, uninstall, dock, hide. */
@Composable
fun AppContextMenu(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val context = LocalContext.current
    val view = LocalView.current
    val app = viewModel.menuApp
    var last by remember { mutableStateOf<AppInfo?>(null) }
    SideEffect { if (app != null) last = app }
    val shown = app ?: last

    AnimatedVisibility(
        visible = app != null,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(140))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.menuApp = null }
                .imePadding()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.popIn()) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    if (shown != null) {
                        MenuContent(viewModel, shown)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuContent(viewModel: LauncherViewModel, shown: AppInfo) {
    val config = viewModel.themeConfig
    val context = LocalContext.current
    val view = LocalView.current
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.78f).dp
    var picking by remember(shown.packageName) { mutableStateOf(false) }
    var newName by remember(shown.packageName) { mutableStateOf("") }
    val inDock = viewModel.isInDock(shown.packageName)
    val currentFolder = viewModel.folderOf(shown.packageName)

    Column(
        modifier = Modifier
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            bitmap = shown.icon,
            contentDescription = shown.label,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Text(
            text = shown.label,
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (!picking) {
            GlassButton("Open", config, Modifier.fillMaxWidth(), Icons.Rounded.PlayArrow) {
                viewModel.launchApp(context, view, shown)
            }
            GlassButton("App info", config, Modifier.fillMaxWidth(), Icons.Rounded.Info) {
                viewModel.openAppInfo(context, shown)
            }
            GlassButton(
                if (currentFolder != null) "Folder: $currentFolder" else "Add to folder",
                config, Modifier.fillMaxWidth(), Icons.Rounded.Folder
            ) { picking = true }
            GlassButton(
                if (inDock) "Remove from dock" else "Add to dock",
                config, Modifier.fillMaxWidth(), Icons.Rounded.PushPin
            ) { viewModel.toggleDock(shown) }
            GlassButton(
                if (viewModel.isDistracting(shown.packageName)) "Unmark distracting" else "Mark as distracting",
                config, Modifier.fillMaxWidth(), Icons.Rounded.NightsStay
            ) { viewModel.toggleDistracting(shown) }
            GlassButton("Text & language", config, Modifier.fillMaxWidth(), Icons.Rounded.TextFields) {
                viewModel.menuApp = null
                viewModel.openTypeStudio(TypeTargets.app(shown.packageName))
            }
            GlassButton("Hide app", config, Modifier.fillMaxWidth(), Icons.Rounded.VisibilityOff) {
                viewModel.hideApp(shown)
            }
            GlassButton(
                "Uninstall", config, Modifier.fillMaxWidth(), Icons.Rounded.Delete,
                tint = Color(0xFFFF6B6B)
            ) { viewModel.uninstall(context, shown) }
        } else {
            Text(
                text = "Choose a folder",
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp
            )
            viewModel.customFolders.forEach { folder ->
                GlassButton(
                    "${folder.name} (${folder.packages.size})",
                    config, Modifier.fillMaxWidth(), Icons.Rounded.Folder
                ) { viewModel.addToFolder(folder.name, shown) }
            }
            LiquidGlassSurface(
                config = config,
                cornerRadius = 18.dp,
                sheen = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = newName,
                    onValueChange = { newName = it.take(20) },
                    placeholder = {
                        Text("New folder name", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    },
                    singleLine = true,
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
            if (newName.isNotBlank()) {
                GlassButton("Create \"${newName.trim()}\"", config, Modifier.fillMaxWidth(), Icons.Rounded.Folder) {
                    viewModel.addToFolder(newName, shown)
                }
            }
            if (currentFolder != null) {
                GlassButton(
                    "Remove from $currentFolder", config, Modifier.fillMaxWidth(), Icons.Rounded.Delete,
                    tint = Color(0xFFFF6B6B)
                ) { viewModel.removeFromFolder(shown) }
            }
            GlassButton("Back", config, Modifier.fillMaxWidth()) { picking = false }
        }
    }
}

/**
 * Private space: apps hidden from the home screen, the drawer and search, behind a PIN.
 * There is no separate OS sandbox here — the apps stay installed, they are simply not
 * shown anywhere until the PIN is entered.
 */
@Composable
fun PrivateSpaceOverlay(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val context = LocalContext.current
    val view = LocalView.current
    var pin by remember { mutableStateOf("") }
    var editingPin by remember { mutableStateOf(false) }
    val hidden = remember(viewModel.hiddenPackages, viewModel.allApps) {
        val byPkg = viewModel.allApps.associateBy { it.packageName }
        viewModel.hiddenPackages.mapNotNull { byPkg[it] }
    }

    AnimatedVisibility(
        visible = viewModel.privateSpaceOpen,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF02060D).copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.privateSpaceOpen = false }
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.popIn()) {
                LiquidGlassSurface(
                    config = config,
                    cornerRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Private space",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!viewModel.privateUnlocked) {
                            Text(
                                text = "Enter the PIN to reveal hidden apps.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            TextField(
                                value = pin,
                                onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                placeholder = {
                                    Text("PIN", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
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
                            if (viewModel.privateError) {
                                Text("Wrong PIN", color = Color(0xFFFF6B6B), fontSize = 12.sp)
                            }
                            GlassButton("Unlock", config, Modifier.fillMaxWidth(), Icons.Rounded.Lock) {
                                if (viewModel.privatePin.isNullOrBlank()) viewModel.savePrivatePin(pin)
                                else viewModel.unlockPrivate(pin)
                                pin = ""
                            }
                            if (viewModel.privatePin.isNullOrBlank()) {
                                Text(
                                    text = "No PIN set yet — the code you type now becomes your PIN.",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            if (hidden.isEmpty()) {
                                Text(
                                    text = "Nothing hidden yet. Long-press any app › Hide app to move it here.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    items(hidden, key = { it.packageName }) { app ->
                                        Box(contentAlignment = Alignment.TopCenter) {
                                            LiquidAppIconItem(
                                                app = app,
                                                config = config,
                                                entranceIndex = 0,
                                                onLongClick = { viewModel.unhideFromPrivate(app) },
                                                onClick = { viewModel.launchApp(context, view, app) }
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Long-press an icon to move it back to the home screen.",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 11.sp
                                )
                            }

                            if (editingPin) {
                                TextField(
                                    value = pin,
                                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    placeholder = {
                                        Text("New PIN", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
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
                                GlassButton("Save PIN", config, Modifier.fillMaxWidth()) {
                                    viewModel.savePrivatePin(pin)
                                    pin = ""
                                    editingPin = false
                                }
                            } else {
                                GlassButton("Change PIN", config, Modifier.fillMaxWidth(), Icons.Rounded.Lock) {
                                    editingPin = true
                                }
                            }
                        }

                        GlassButton("Close", config, Modifier.fillMaxWidth()) {
                            viewModel.privateSpaceOpen = false
                        }
                    }
                }
            }
        }
    }
}
