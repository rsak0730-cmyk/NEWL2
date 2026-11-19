package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.fonts.CatalogFont
import com.manish.liquidlauncher.fonts.FontLibrary
import com.manish.liquidlauncher.fonts.InstalledFont
import com.manish.liquidlauncher.fonts.Languages
import com.manish.liquidlauncher.model.FontChoice
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

/**
 * Type & Language Studio.
 *
 * Every piece of launcher text (each folder, each app label, the dock, the widgets, the drawer)
 * can carry its own font and its own language. Fonts are searched by name, filtered by any
 * language Google Fonts publishes, downloaded on demand and then applied to the chosen target.
 */
@Composable
fun TypeStudioSheet(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp
    val target = viewModel.typeStudioTarget
    val current = viewModel.typeMap[target]

    AnimatedVisibility(
        visible = viewModel.typeStudioOpen,
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
                ) { viewModel.typeStudioOpen = false }
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Type & Language",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Editing: " + TypeTargets.label(target),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row {
                                IconButton(onClick = { viewModel.searchFonts() }) {
                                    Icon(Icons.Rounded.Refresh, "Reload catalogue", tint = accent)
                                }
                                IconButton(onClick = { viewModel.typeStudioOpen = false }) {
                                    Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                                }
                            }
                        }

                        // ---- Target picker ----
                        StudioLabel("Apply to", accent)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val targets = remember(viewModel.customFolders) {
                                val list = mutableListOf(
                                    TypeTargets.HOME,
                                    TypeTargets.DOCK,
                                    TypeTargets.DRAWER,
                                    TypeTargets.CLOCK,
                                    TypeTargets.BATTERY,
                                    TypeTargets.WEATHER,
                                    TypeTargets.MUSIC,
                                    TypeTargets.TIMELINE
                                )
                                viewModel.customFolders.forEach { list.add(TypeTargets.folder(it.name)) }
                                list
                            }
                            targets.forEach { t ->
                                TargetChip(
                                    label = TypeTargets.label(t),
                                    selected = t == target,
                                    accent = accent,
                                    onClick = { viewModel.openTypeStudio(t) }
                                )
                            }
                        }

                        // ---- Current selection ----
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Language, null, tint = accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = current?.let { "${it.familyName}${if (it.languageTag.isNotBlank()) " · " + languageName(it.languageTag) else ""}" }
                                    ?: "Following the launcher default",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (current != null) {
                                Text(
                                    text = "Reset",
                                    color = accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { viewModel.clearType(target) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // ---- Language filter ----
                        StudioLabel("Language", accent)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TargetChip(
                                label = "Any language",
                                selected = viewModel.fontLanguage.isBlank(),
                                accent = accent
                            ) { viewModel.chooseFontLanguage("") }
                            Languages.all.forEach { lang ->
                                TargetChip(
                                    label = lang.native,
                                    selected = viewModel.fontLanguage == lang.tag,
                                    accent = accent
                                ) { viewModel.chooseFontLanguage(lang.tag) }
                            }
                        }

                        // ---- Search by font name ----
                        StudioLabel("Search fonts by name", accent)
                        LiquidGlassSurface(
                            config = config,
                            cornerRadius = 18.dp,
                            sheen = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = viewModel.fontQuery,
                                    onValueChange = { viewModel.fontQuery = it },
                                    placeholder = {
                                        Text(
                                            "e.g. Noto Sans Tamil, Poppins…",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 13.sp
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { viewModel.searchFonts() }),
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
                                IconButton(onClick = { viewModel.searchFonts() }) {
                                    if (viewModel.fontSearching) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = accent
                                        )
                                    } else {
                                        Icon(Icons.Rounded.Search, "Search fonts", tint = accent)
                                    }
                                }
                            }
                        }

                        val note = viewModel.fontNote
                        if (note != null) {
                            Text(
                                text = note,
                                color = if (note.contains("installed", true)) accent else Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        }

                        // ---- Online results ----
                        if (viewModel.fontResults.isNotEmpty()) {
                            StudioLabel("Online catalogue (${viewModel.fontResults.size})", accent)
                            viewModel.fontResults.take(30).forEach { font ->
                                OnlineFontRow(
                                    font = font,
                                    accent = accent,
                                    busy = viewModel.fontBusyId == font.id,
                                    installed = FontLibrary.isInstalled(font.id),
                                    sample = Languages.sample(
                                        viewModel.fontLanguage.ifBlank { font.languageTag }
                                    ),
                                    onDownload = { viewModel.installFont(font) },
                                    onApply = {
                                        viewModel.applyType(
                                            target,
                                            FontChoice(
                                                fontId = font.id,
                                                familyName = font.family,
                                                languageTag = viewModel.fontLanguage.ifBlank { font.languageTag },
                                                script = font.script
                                            )
                                        )
                                    }
                                )
                            }
                            if (viewModel.fontResults.size > 30) {
                                Text(
                                    text = "Showing the first 30 of ${viewModel.fontResults.size}. Type a name to narrow it down.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // ---- Installed ----
                        StudioLabel("Installed (${FontLibrary.installedFonts().size})", accent)
                        FontLibrary.installedFonts().forEach { font ->
                            InstalledFontRow(
                                font = font,
                                accent = accent,
                                selected = current?.fontId == font.id,
                                sample = Languages.sample(
                                    viewModel.fontLanguage.ifBlank { font.languageTag }
                                ),
                                onApply = {
                                    viewModel.applyType(
                                        target,
                                        FontChoice(
                                            fontId = font.id,
                                            familyName = font.family,
                                            languageTag = viewModel.fontLanguage.ifBlank { font.languageTag },
                                            script = font.script
                                        )
                                    )
                                },
                                onDelete = if (font.isSystem) null else ({ viewModel.removeFont(font.id) })
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        GlassButton(
                            text = "Apply to every label",
                            config = config,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Check
                        ) {
                            current?.let { viewModel.applyType(TypeTargets.HOME, it) }
                        }
                        Text(
                            text = "Fonts are downloaded from the public Google Fonts library into the launcher's own storage, so they keep working offline. No account needed.",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioLabel(text: String, accent: Color) {
    Text(
        text = text.uppercase(),
        color = accent.copy(alpha = 0.85f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun TargetChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .jellyTouch(haptics = true, pressX = 0.94f, pressY = 0.9f, onClick = onClick)
            .background(
                if (selected) accent.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.09f),
                CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun OnlineFontRow(
    font: CatalogFont,
    accent: Color,
    busy: Boolean,
    installed: Boolean,
    sample: String,
    onDownload: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.06f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sample,
                color = Color.White,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${font.family} · ${font.script}",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
        } else if (installed) {
            SmallPill(text = "Apply", accent = accent, onClick = onApply)
        } else {
            IconButton(onClick = onDownload, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.Download, "Download ${font.family}", tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun InstalledFontRow(
    font: InstalledFont,
    accent: Color,
    selected: Boolean,
    sample: String,
    onApply: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val family: FontFamily = remember(font.id) { FontLibrary.familyForId(font.id) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f),
                CircleShape
            )
            .jellyTouch(haptics = true, pressX = 0.98f, pressY = 0.94f, onClick = onApply)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sample,
                color = Color.White,
                fontFamily = family,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = font.family,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (selected) {
            Icon(Icons.Rounded.Check, "Applied", tint = accent, modifier = Modifier.size(18.dp))
        } else if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.Delete, "Remove ${font.family}", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SmallPill(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.28f), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun languageName(tag: String): String =
    Languages.byTag(tag)?.label ?: tag

/** Font preview string for a language tag, exposed for other screens. */
fun typeSampleFor(tag: String): String =
    if (tag.isBlank()) "Aa 12:30" else Languages.sample(tag)
