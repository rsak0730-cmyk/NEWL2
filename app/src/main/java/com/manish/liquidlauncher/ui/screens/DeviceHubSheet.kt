package com.manish.liquidlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

/**
 * Multi-device hub.
 *
 * There is no cloud account behind this launcher, so "multi-device" means one concrete,
 * working thing: your whole layout — theme, dock, folders, grid, gestures, fonts, private
 * PIN — travels to another phone as a single file, through whatever channel you already
 * have (Nearby Share, Bluetooth, email, Drive). The other phone opens the same file with
 * "Receive a layout".
 */
@Composable
fun DeviceHubSheet(viewModel: LauncherViewModel, onImport: () -> Unit) {
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val context = LocalContext.current
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.8f).dp

    AnimatedVisibility(
        visible = viewModel.deviceHubOpen,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.deviceHubOpen = false }
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(18.dp),
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
                            Text(
                                text = "Multi-device hub",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.deviceHubOpen = false }) {
                                Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                            }
                        }

                        Text(
                            text = "This device",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        InfoRow("Name", "${Build.MANUFACTURER} ${Build.MODEL}", accent)
                        InfoRow("Android", "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})", accent)
                        InfoRow("Layout", "${viewModel.installedApps.size} apps · ${viewModel.customFolders.size} folders", accent)
                        InfoRow("Fonts", "${viewModel.installedFonts().size} installed", accent)

                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Move your layout",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Send your theme, dock, folders, grid, gestures, fonts and private PIN to another phone as one file. Nothing is uploaded anywhere — the file goes through the share sheet you pick.",
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = 11.sp
                        )
                        GlassButton(
                            "Send layout to another device",
                            config,
                            Modifier.fillMaxWidth(),
                            Icons.Rounded.Share
                        ) {
                            viewModel.shareLayout(context)
                        }
                        GlassButton(
                            "Receive a layout on this device",
                            config,
                            Modifier.fillMaxWidth(),
                            Icons.Rounded.Public
                        ) {
                            viewModel.deviceHubOpen = false
                            onImport()
                        }
                        GlassButton(
                            "Refresh device info",
                            config,
                            Modifier.fillMaxWidth(),
                            Icons.Rounded.Refresh
                        ) {
                            viewModel.loadInstalledApps()
                        }

                        Text(
                            text = "Live two-way sync would need a companion app and a server; this hub deliberately does not pretend to have one.",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Writes the layout JSON to the cache dir and hands it to the system share sheet. */
fun shareLayoutFile(context: Context, json: String) {
    try {
        val dir = java.io.File(context.cacheDir, "share").apply { mkdirs() }
        val file = java.io.File(dir, "liquid-launcher-layout.json")
        file.writeText(json)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Liquid launcher layout")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Send layout to…").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Throwable) {
        android.widget.Toast
            .makeText(context, "Couldn't share the layout on this device", android.widget.Toast.LENGTH_SHORT)
            .show()
    }
}
