package com.manish.liquidlauncher.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.ui.components.GlassButton
import com.manish.liquidlauncher.ui.components.LiquidGlassSurface
import com.manish.liquidlauncher.ui.components.jellyTouch
import com.manish.liquidlauncher.viewmodel.LauncherViewModel

/** First-launch walkthrough: introduces the liquid-glass gestures in three glass cards. */
@Composable
fun OnboardingScreen(viewModel: LauncherViewModel) {
    val config = viewModel.themeConfig
    val step by remember { mutableIntStateOf(0) }
    val total = 3

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Liquid Glass",
            body = "A dark, glassy launcher where every surface moves with light. Long-press an icon to edit, drag to rearrange, drag over another app to make a folder.",
            icon = Icons.Rounded.Folder
        ),
        OnboardingPage(
            title = "Gestures & Themes",
            body = "Swipe up for the drawer, double tap to lock, long press for Theme Engine. Pick any hue, any glass density, and match your wallpaper with one tap.",
            icon = Icons.Rounded.Gesture
        ),
        OnboardingPage(
            title = "Privacy first",
            body = "No accounts, no servers, no tracking. Weather and fonts are the only network calls. Your layout stays on your device — back it up as a JSON file whenever you like.",
            icon = Icons.Rounded.Lock
        )
    )

    AnimatedVisibility(
        visible = viewModel.onboardingOpen,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(250))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LiquidGlassSurface(
                config = config,
                cornerRadius = 34.dp,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(520.dp)
                    .jellyTouch(haptics = config.haptics, pressX = 0.92f, pressY = 0.92f, onClick = {})
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.hsv(195f, 0.8f, 1f),
                                        Color.hsv(245f, 0.8f, 0.9f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = pages[step].icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = pages[step].title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pages[step].body,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (step > 0) {
                            GlassButton(
                                text = "Back",
                                config = config,
                                modifier = Modifier.weight(1f)
                            ) { step = step - 1 }
                        } else {
                            Box(modifier = Modifier.width(72.dp))
                        }
                        if (step < total - 1) {
                            GlassButton(
                                text = "Next",
                                config = config,
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.ArrowForward
                            ) { step = step + 1 }
                        } else {
                            GlassButton(
                                text = "Get started",
                                config = config,
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.ArrowForward
                            ) {
                                viewModel.onboardingComplete()
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
