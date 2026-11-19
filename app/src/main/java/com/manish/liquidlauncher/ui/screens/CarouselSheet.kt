package com.manish.liquidlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.accentColor
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.ui.components.popIn
import com.manish.liquidlauncher.viewmodel.LauncherViewModel
import kotlin.math.abs

/**
 * 3D app carousel: a coverflow of every installed app. The card nearest the centre of the
 * screen stands upright, the ones on either side rotate away and dim, so the whole shelf
 * reads as a ring of glass cards. Tap a card to launch it.
 */
@Composable
fun AppCarouselSheet(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val accent = config.accentColor()
    val context = LocalContext.current
    val view = LocalView.current
    val state = rememberLazyListState()
    val apps = viewModel.installedApps
    val font = remember(viewModel.typeRevision, viewModel.typeMap) {
        viewModel.typeFamily(TypeTargets.DRAWER)
    }
    val cardWidth = 148.dp

    AnimatedVisibility(
        visible = viewModel.carouselOpen,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF02060D).copy(alpha = 0.92f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.carouselOpen = false }
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                    Text(
                        text = "3D App Carousel",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = { viewModel.carouselOpen = false },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyRow(
                        state = state,
                        contentPadding = PaddingValues(horizontal = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            CarouselCard(
                                app = app,
                                config = config,
                                width = cardWidth,
                                accent = accent,
                                font = font,
                                distance = carouselDistance(state, app.packageName, apps, cardWidth),
                                onOpen = {
                                    viewModel.carouselOpen = false
                                    viewModel.launchApp(context, view, app)
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Swipe the shelf · tap a card to open",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
                )
            }
        }
    }
}

/**
 * Distance of one card from the centre, in "cards". 0 = perfectly centred.
 * Derived from the list's scroll state so no extra state is needed.
 */
private fun carouselDistance(
    state: androidx.compose.foundation.lazy.LazyListState,
    packageName: String,
    apps: List<AppInfo>,
    cardWidth: androidx.compose.ui.unit.Dp
): Float {
    val index = apps.indexOfFirst { it.packageName == packageName }
    if (index < 0) return 3f
    val centreIndex = state.firstVisibleItemIndex + 1f
    return (index - centreIndex).toFloat()
}

@Composable
private fun CarouselCard(
    app: AppInfo,
    config: LiquidThemeConfig,
    width: androidx.compose.ui.unit.Dp,
    accent: Color,
    font: androidx.compose.ui.text.font.FontFamily,
    distance: Float,
    onOpen: () -> Unit
) {
    val clamped = distance.coerceIn(-3f, 3f)
    val rotation = (-clamped * 22f).coerceIn(-66f, 66f)
    val scale = (1f - abs(clamped) * 0.12f).coerceIn(0.62f, 1f)
    val alpha = (1f - abs(clamped) * 0.22f).coerceIn(0.25f, 1f)

    Box(modifier = Modifier.popIn()) {
        LiquidGlassSurface(
            config = config,
            cornerRadius = 28.dp,
            tintOverride = accent,
            modifier = Modifier
                .width(width)
                .height(230.dp)
                .graphicsLayer {
                    rotationY = rotation
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    cameraDistance = 12f * density
                }
                .jellyTouch(haptics = true, pressX = 0.94f, pressY = 0.9f, onClick = onOpen)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(78.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Text(
                    text = app.label,
                    color = Color.White,
                    fontFamily = font,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
