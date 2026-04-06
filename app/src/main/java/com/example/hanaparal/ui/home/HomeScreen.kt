package com.example.hanaparal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.model.StudyGroupItem

@Composable
fun HomeScreen(
    userName: String = "Student",
    userCourse: String = "",
    userInitial: String = "S",
    isCreateGroupEnabled: Boolean,
    maxMembersPerGroup: Int = 10,
    announcementHeader: String = "Welcome to HanapAral",
    // Navigation actions
    onUnlockClick: () -> Unit,
    onCreateGroup: () -> Unit = {},
    onViewAllGroups: () -> Unit = {},
    onGroupClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    myGroups: List<StudyGroupItem> = emptyList(),
    suggestedGroups: List<StudyGroupItem> = emptyList()
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                userName = userName,
                userInitial = userInitial,
                onNotificationClick = onNotificationClick,
                onSignOut = onSignOut,
                onSettingsClick = onSettingsClick
            )
        },
        floatingActionButton = {
            // Initially disabled/locked until Biometric Scan
            ExtendedFloatingActionButton(
                onClick = {
                    if (isCreateGroupEnabled) onCreateGroup() else onUnlockClick()
                },
                containerColor = if (isCreateGroupEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                icon = {
                    // Changes icon based on biometric lock state
                    Icon(
                        imageVector = if (isCreateGroupEnabled) Icons.Outlined.Add else Icons.Outlined.Lock,
                        contentDescription = null
                    )
                },
                text = {
                    // Changes text to guide the student
                    Text(
                        text = if (isCreateGroupEnabled) "Create Group" else "Scan to Unlock",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            //  UI reacts to Remote Config "global_announcement"
            AnnouncementBanner(message = announcementHeader)

            WelcomeCard(name = userName, course = userCourse)

            QuickActionsSection(
                isCreateGroupEnabled = isCreateGroupEnabled,
                onCreateGroup = onCreateGroup,
                onUnlockClick = onUnlockClick,
                onViewAllGroups = onViewAllGroups
            )

            StudyGroupSection(
                title = "My Study Groups",
                groups = myGroups,
                emptyMessage = "You have not joined any groups yet.",
                onGroupClick = onGroupClick,
                onViewAll = onViewAllGroups
            )

            // UI shows "Max Members" from Remote Config
            GroupStatsCard(
                myGroupCount = myGroups.size,
                maxMembersPerGroup = maxMembersPerGroup
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    userName: String,
    userInitial: String,
    onNotificationClick: () -> Unit,
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "HanapAral",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = onSignOut) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userInitial, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun AnnouncementBanner(message: String) {
    if (message.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WelcomeCard(name: String, course: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Good day, ${name.split(" ").firstOrNull() ?: name}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (course.isNotBlank()) {
                Text(text = course, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    isCreateGroupEnabled: Boolean,
    onCreateGroup: () -> Unit,
    onUnlockClick: () -> Unit,
    onViewAllGroups: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Quick Actions", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = if (isCreateGroupEnabled) Icons.Outlined.GroupAdd else Icons.Outlined.Lock,
                label = if (isCreateGroupEnabled) "Create" else "Unlock",
                onClick = { if (isCreateGroupEnabled) onCreateGroup() else onUnlockClick() },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Outlined.Search,
                label = "Browse",
                onClick = onViewAllGroups,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StudyGroupSection(title: String, groups: List<StudyGroupItem>, emptyMessage: String, onGroupClick: (String) -> Unit, onViewAll: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) { Text("View all") }
        }
        if (groups.isEmpty()) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {

        }
    }
}

@Composable
private fun GroupStatsCard(myGroupCount: Int, maxMembersPerGroup: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = myGroupCount.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("My Groups", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = maxMembersPerGroup.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Max Limit", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}