@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    config: LiquidThemeConfig,
    cornerRadius: Dp = config.cornerRadiusDp.dp,
    tintOverride: Color? = null,
    sheen: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val accent = tintOverride ?: config.accentColor()
    val clock = LocalAnimClock.current

    val pulse = (sin(clock.value * 1.4f) + 1f) / 2f
    val lift = 8.dp + (pulse * 2f).dp

    Box(
        modifier = modifier
            .shadow(
                elevation = lift,
                shape = shape,
                ambientColor = Color(0xFFB9C2D8).copy(alpha = 0.65f),
                spotColor = Color(0xFF9DAAC8).copy(alpha = 0.42f)
            )
            .clip(shape)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val stroke = 1.dp.toPx()
                val radius = cornerRadius.toPx()

                val light = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF5F6FB),
                        accent.copy(alpha = 0.10f),
                        Color(0xFFE9ECF5)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )

                val highlight = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.65f
                )

                val rim = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.65f),
                        accent.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.9f)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, h)
                )

                onDrawWithContent {
                    drawRect(light)
                    drawRect(highlight)

                    drawContent()

                    if (sheen) {
                        val bandWidth = w * 0.30f
                        val cycle = (clock.value * 0.16f) % 2f

                        if (cycle < 1f) {
                            val x = -bandWidth + cycle * (w + bandWidth * 2f)

                            rotate(
                                degrees = 18f,
                                pivot = Offset(w / 2f, h / 2f)
                            ) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                    ),
                                    topLeft = Offset(x, -h),
                                    size = Size(bandWidth, h * 3f)
                                )
                            }
                        }
                    }

                    drawRoundRect(
                        brush = rim,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(w - stroke, h - stroke),
                        cornerRadius = CornerRadius(
                            (radius - stroke).coerceAtLeast(0f)
                        ),
                        style = Stroke(width = stroke)
                    )
                }
            },
        content = content
    )
}