package com.manish.liquidlauncher.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.ui.theme.LiquidGlassTheme

class FeatureStudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiquidGlassTheme {
                FeatureStudioScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureStudioScreen(onBack: () -> Unit) {
    var expanded by remember { mutableStateOf(setOf<String>()) }
    val bg = Brush.linearGradient(
        listOf(Color(0xFF071323), Color(0xFF17102D), Color(0xFF061C29))
    )
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NEW-LAUNCHER", color = Color.White)
                        Text(
                            "${LauncherFeatureCatalog.all.size} features · " +
                                "${LauncherFeatureCatalog.count(LauncherFeatureCatalog.Status.Ready)} ready",
                            color = Color.White.copy(.62f), fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xCC0B1020))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(bg).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color.White.copy(.07f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Text("Liquid Feature Studio", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Every feature below is wired into the launcher. The badge says how far it goes on this device: READY works everywhere, NEEDS PERMISSION needs one Android grant, DEVICE DEPENDENT depends on OEM multi-window support, PLANNED is not built yet.",
                        color = Color.White.copy(.72f), style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Prediction and recommendations stay rule-based and on-device. No usage data leaves the phone.",
                        color = Color(0xFF9DE7FF), style = MaterialTheme.typography.labelMedium)
                }
            }
            items(LauncherFeatureCatalog.groups, key = { it.first }) { (group, items) ->
                var open by remember { mutableStateOf(true) }
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color.White.copy(.065f), RoundedCornerShape(22.dp))
                        .animateContentSize()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(group, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("${items.size}", color = Color(0xFF9DE7FF), style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { open = !open }) {
                            Icon(Icons.Rounded.ExpandMore, contentDescription = "Expand", tint = Color.White)
                        }
                    }
                    AnimatedVisibility(open) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { feature ->
                                var detailOpen by remember { mutableStateOf(false) }
                                Column(
                                    Modifier.fillMaxWidth()
                                        .background(Color.Black.copy(.16f), RoundedCornerShape(15.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(feature.name, color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            feature.status.label,
                                            color = when (feature.status) {
                                                LauncherFeatureCatalog.Status.Ready -> Color(0xFF7DFFB3)
                                                LauncherFeatureCatalog.Status.NeedsPermission -> Color(0xFFFFD166)
                                                LauncherFeatureCatalog.Status.DeviceDependent -> Color(0xFF9DE7FF)
                                                LauncherFeatureCatalog.Status.Planned -> Color(0xFFFF9AA2)
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text(feature.description, color = Color.White.copy(.60f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Nothing here is marked PLANNED any more: every feature on this screen is built. DEVICE DEPENDENT means the launcher does its part and the rest is up to the OEM (free-form windows, split-screen pairs, the system recents list). NEEDS PERMISSION means one Android grant, e.g. notification access for the music widgets.",
                    color = Color.White.copy(.55f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
            }
        }
    }
}
