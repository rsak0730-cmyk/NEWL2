package com.manish.liquidlauncher

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.manish.liquidlauncher.ui.components.LocalAnimClock
import com.manish.liquidlauncher.ui.components.LocalTilt
import com.manish.liquidlauncher.ui.components.rememberAnimClock
import com.manish.liquidlauncher.ui.components.rememberTilt
import com.manish.liquidlauncher.ui.screens.AppCarouselSheet
import com.manish.liquidlauncher.ui.screens.AppContextMenu
import com.manish.liquidlauncher.ui.screens.AppDrawerSheet
import com.manish.liquidlauncher.ui.screens.DeviceHubSheet
import com.manish.liquidlauncher.ui.screens.FolderOverlay
import com.manish.liquidlauncher.ui.screens.GestureStudioSheet
import com.manish.liquidlauncher.ui.screens.HomeScreen
import com.manish.liquidlauncher.ui.screens.IdleScreen
import com.manish.liquidlauncher.ui.screens.OnboardingScreen
import com.manish.liquidlauncher.ui.screens.PrivateSpaceOverlay
import com.manish.liquidlauncher.ui.screens.ThemeEngineSheet
import com.manish.liquidlauncher.ui.screens.TypeStudioSheet
import com.manish.liquidlauncher.ui.theme.LiquidGlassTheme
import com.manish.liquidlauncher.ui.theme.isDarkMode
import com.manish.liquidlauncher.util.SoundFx
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            LiquidLauncherRoot(viewModel)
        }
    }

    // singleTask: pressing Home while we're already running lands here
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onHomePressed()
    }
}

@Composable
fun LiquidLauncherRoot(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val config = viewModel.themeConfig

        LiquidGlassTheme(isDark = config.themeMode.isDarkMode(context)) {
            SideEffect {
                SoundFx.enabled = config.sounds
                SoundFx.theme = config.soundTheme
            }

    // Persistable permission keeps the wallpaper working after reboot
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
            viewModel.setWallpaper(uri)
        }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) viewModel.exportBackup(context, uri)
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.importBackup(context, uri)
    }
    val pickWallpaper: () -> Unit = { wallpaperPicker.launch(arrayOf("image/*")) }

    BackHandler(enabled = true) { viewModel.handleBack() }

    val drawerOpen = viewModel.isDrawerOpen
    // Freeze the animation loop while the (blurred) home is hidden behind the drawer to save GPU/battery
    val clock = rememberAnimClock(running = config.liveAnimations && !drawerOpen)
    val tilt = rememberTilt(enabled = config.tiltEffects && !drawerOpen)

    val blurRadius by animateDpAsState(
        targetValue = if (drawerOpen && config.blurDrawer) 22.dp else 0.dp,
        animationSpec = tween(380),
        label = "homeBlur"
    )
    val homeScale by animateFloatAsState(
        targetValue = if (drawerOpen) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "homeScale"
    )

    CompositionLocalProvider(LocalAnimClock provides clock, LocalTilt provides tilt) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF030712))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = homeScale
                        scaleY = homeScale
                    }
                    .then(if (blurRadius > 0.5.dp) Modifier.blur(blurRadius) else Modifier)
            ) {
                HomeScreen(viewModel = viewModel, onPickWallpaper = pickWallpaper)
            }
            AppDrawerSheet(viewModel = viewModel)
            FolderOverlay(viewModel = viewModel)
            ThemeEngineSheet(
                viewModel = viewModel,
                onPickWallpaper = pickWallpaper,
                onExport = { exportPicker.launch("liquid-launcher-backup.json") },
                onImport = { importPicker.launch(arrayOf("application/json", "text/plain", "*/*")) }
            )
            AppContextMenu(viewModel = viewModel)
            TypeStudioSheet(viewModel = viewModel)
            AppCarouselSheet(viewModel = viewModel)
            DeviceHubSheet(
                viewModel = viewModel,
                onImport = { importPicker.launch(arrayOf("application/json", "text/plain", "*/*")) }
            )
            GestureStudioSheet(viewModel = viewModel)
            PrivateSpaceOverlay(viewModel = viewModel)
            IdleScreen(viewModel = viewModel)
            if (viewModel.onboardingOpen) {
                OnboardingScreen(viewModel = viewModel)
            }
        }
    }
} // close LiquidGlassTheme
} // close LiquidLauncherRoot
