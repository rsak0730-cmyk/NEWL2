package com.manish.liquidlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.ui.theme.LiquidTypography

/**
 * Empty state with helpful message and optional action button.
 *
 * UX guidelines applied:
 * - Shows helpful message and action when no content exists (rule 79)
 * - Clear visual hierarchy with icon, title, description
 * - Optional primary action to guide user forward
 */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.Inbox,
    title: String = "Nothing here yet",
    description: String = "Start by adding items from the app drawer.",
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    config: LiquidThemeConfig,
    modifier: Modifier = Modifier
) {
    val accent = config.accentColor()
    val isDark = config.themeMode == com.manish.liquidlauncher.model.ThemeMode.Dark
    val textColor = if (isDark) Color(0xFFF1F2F6) else Color(0xFF343849)
    val mutedColor = if (isDark) Color(0xFFB9C2D8) else Color(0xFF858B9C)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDark) Color(0xFF0A101B) else Color(0xFFF7F8FC)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
                    )
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = LiquidTypography.titleMedium,
                color = textColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = LiquidTypography.bodyMedium,
                color = mutedColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .maxWidth(260.dp)
            )

            if (actionLabel != null && onActionClick != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))

                GlassButton(
                    text = actionLabel!!,
                    config = config,
                    modifier = Modifier.width(140.dp),
                    onClick = onActionClick
                )
            }
        }
    }
}
