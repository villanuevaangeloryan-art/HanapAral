package com.example.hanaparal.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(
    uid          : String,
    email        : String,
    onProfileSaved: () -> Unit = {},
    viewModel    : ProfileViewModel = viewModel()
) {
    var name         by remember { mutableStateOf("") }
    var course       by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var isFetching   by remember { mutableStateOf(true) }
    var statusMsg    by remember { mutableStateOf("") }
    var isSuccess    by remember { mutableStateOf(false) }
    var profileSaved by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid) { student ->
            student?.let {
                name   = it.name
                course = it.course
                if (it.name.isNotBlank()) profileSaved = true
            }
            isFetching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isFetching) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text  = "Loading your profile...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Header ────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn() + slideInVertically(initialOffsetY = { -30 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = if (name.isNotBlank()) name.take(1).uppercase() else "S",
                                style      = MaterialTheme.typography.displayMedium,
                                color      = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text       = if (profileSaved) "Edit Profile" else "Create Profile",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text      = "Your information will be stored securely\nand linked to your study groups.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))

                // ── Section label ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text     = "  Personal Information  ",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Full Name ─────────────────────────────────────────────
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it; statusMsg = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Full Name") },
                    placeholder   = { Text("e.g. Juan Dela Cruz") },
                    leadingIcon   = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(16.dp))

                // ── Course ────────────────────────────────────────────────
                OutlinedTextField(
                    value         = course,
                    onValueChange = { course = it; statusMsg = "" },
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Course / Program") },
                    placeholder   = { Text("e.g. BS Information Technology") },
                    leadingIcon   = {
                        Icon(
                            Icons.Outlined.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        focusedLabelColor    = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(16.dp))

                // ── Email (read only) ─────────────────────────────────────
                OutlinedTextField(
                    value         = email,
                    onValueChange = {},
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text("Email Address") },
                    leadingIcon   = {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    enabled    = false,
                    singleLine = true,
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor      = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledLabelColor       = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor        = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text     = "Automatically filled from your Google account",
                    modifier = Modifier.fillMaxWidth(),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(28.dp))

                // ── Status message ────────────────────────────────────────
                AnimatedVisibility(visible = statusMsg.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = if (isSuccess)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text       = statusMsg,
                            modifier   = Modifier.padding(14.dp),
                            color      = if (isSuccess)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Save button ───────────────────────────────────────────
                Button(
                    onClick = {
                        if (name.isBlank() || course.isBlank()) {
                            isSuccess = false
                            statusMsg = "Please fill in your name and course."
                            return@Button
                        }
                        isLoading = true
                        viewModel.saveProfile(uid, name, course, email) { success ->
                            isLoading    = false
                            isSuccess    = success
                            profileSaved = success
                            statusMsg    = if (success)
                                "Profile saved successfully."
                            else
                                "Save failed. Please try again."
                            if (success) onProfileSaved()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape   = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = if (profileSaved) "Update Profile" else "Save Profile",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}