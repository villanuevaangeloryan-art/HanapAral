package com.example.hanaparal.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onSignInClick: () -> Unit) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── App icon ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600)) + slideInVertically(
                    animationSpec  = tween(600),
                    initialOffsetY = { -60 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Group,
                            contentDescription = null,
                            modifier           = Modifier.size(52.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text       = "HanapAral",
                        style      = MaterialTheme.typography.displayMedium,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text      = "Cloud Based Integrated\nStudy Group Finder",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(48.dp))

                    // ── Feature highlights ────────────────────────────────
                    FeatureRow(
                        icon  = Icons.Outlined.Group,
                        title = "Find Study Groups",
                        desc  = "Discover and join groups that match your course"
                    )

                    Spacer(Modifier.height(12.dp))

                    FeatureRow(
                        icon  = Icons.Outlined.Group,
                        title = "Collaborate Online",
                        desc  = "Work together with classmates in the cloud"
                    )

                    Spacer(Modifier.height(12.dp))

                    FeatureRow(
                        icon  = Icons.Outlined.Group,
                        title = "Stay Updated",
                        desc  = "Get real-time notifications from your groups"
                    )

                    Spacer(Modifier.height(48.dp))

                    // ── Sign in button ────────────────────────────────────
                    OutlinedButton(
                        onClick  = onSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "G",
                                style      = MaterialTheme.typography.labelLarge,
                                color      = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = "Continue with Google",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text      = "By signing in, you agree to our Terms of Service\nand Privacy Policy.",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon  : androidx.compose.ui.graphics.vector.ImageVector,
    title : String,
    desc  : String
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(22.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                color      = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text  = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
