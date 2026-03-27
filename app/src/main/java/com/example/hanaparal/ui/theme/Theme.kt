package com.example.hanaparal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Navy700,
    onPrimary          = White,
    primaryContainer   = Navy100,
    onPrimaryContainer = Gray900,

    secondary            = Teal500,
    onSecondary          = White,
    secondaryContainer   = TealLight,
    onSecondaryContainer = Gray900,

    background   = OffWhite,
    onBackground = Gray900,

    surface          = Surface,
    onSurface        = Gray900,
    surfaceVariant   = Gray100,
    onSurfaceVariant = Gray700,

    outline  = Outline,
    error    = Error,
    onError  = White
)

private val DarkColorScheme = darkColorScheme(
    primary            = Navy500,
    onPrimary          = White,
    primaryContainer   = Navy700,
    onPrimaryContainer = White,

    secondary            = Teal500,
    onSecondary          = White,
    secondaryContainer   = androidx.compose.ui.graphics.Color(0xFF004D40),
    onSecondaryContainer = TealLight,

    background   = androidx.compose.ui.graphics.Color(0xFF0D0D1A),
    onBackground = White,

    surface          = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
    onSurface        = White,
    surfaceVariant   = androidx.compose.ui.graphics.Color(0xFF252540),
    onSurfaceVariant = Gray300,

    outline  = androidx.compose.ui.graphics.Color(0xFF3A3A55),
    error    = Error,
    onError  = White
)

@Composable
fun HanapAralTheme(
    darkTheme    : Boolean = isSystemInDarkTheme(),
    dynamicColor : Boolean = false,
    content      : @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}