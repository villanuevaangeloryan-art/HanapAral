package com.example.hanaparal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.hanaparal.data.model.Group

@Composable
fun HomeScreen(
    userName: String = "Student",
    userCourse: String = "",
    userInitial: String = "S",
    isCreateGroupEnabled: Boolean,
    maxMembersPerGroup: Int = 10,
    announcementHeader: String = "Welcome to HanapAral",
    onUnlockClick: () -> Unit,
    onCreateGroup: () -> Unit = {},
    onViewAllGroups: () -> Unit = {},
    onGroupClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    myGroups: List<Group> = emptyList()
) {
    Scaffold(
        topBar = {
            HomeTopBar(userName, userInitial, onNotificationClick, onSignOut, onSettingsClick)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (isCreateGroupEnabled) onCreateGroup() else onUnlockClick() },
                containerColor = if (isCreateGroupEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                icon = { Icon(if (isCreateGroupEnabled) Icons.Outlined.Add else Icons.Outlined.Lock, null) },
                text = { Text(if (isCreateGroupEnabled) "Create Group" else "Scan to Unlock", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            AnnouncementBanner(announcementHeader)
            WelcomeCard(userName, userCourse)
            QuickActionsSection(isCreateGroupEnabled, onCreateGroup, onUnlockClick, onViewAllGroups)

            StudyGroupSection(
                title = "My Study Groups",
                groups = myGroups,
                emptyMessage = "You have not joined any groups yet.",
                onGroupClick = onGroupClick,
                onViewAll = onViewAllGroups
            )

            GroupStatsCard(myGroups.size, maxMembersPerGroup)
            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(userName: String, userInitial: String, onNotificationClick: () -> Unit, onSignOut: () -> Unit, onSettingsClick: () -> Unit) {
    TopAppBar(
        title = { Text("HanapAral", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        actions = {
            IconButton(onClick = onSettingsClick) { Icon(Icons.Outlined.Settings, "Settings") }
            IconButton(onClick = onNotificationClick) { Icon(Icons.Outlined.Notifications, "Notifications") }
            IconButton(onClick = onSignOut) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(userInitial, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun AnnouncementBanner(message: String) {
    if (message.isBlank()) return
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Campaign, null, tint = MaterialTheme.colorScheme.secondary)
            Text(message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WelcomeCard(name: String, course: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Column(Modifier.padding(20.dp)) {
            Text("Good day, ${name.split(" ").firstOrNull() ?: name}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (course.isNotBlank()) Text(course, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun QuickActionsSection(isCreateGroupEnabled: Boolean, onCreateGroup: () -> Unit, onUnlockClick: () -> Unit, onViewAllGroups: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Quick Actions", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(if (isCreateGroupEnabled) Icons.Outlined.GroupAdd else Icons.Outlined.Lock, if (isCreateGroupEnabled) "Create" else "Unlock", { if (isCreateGroupEnabled) onCreateGroup() else onUnlockClick() }, Modifier.weight(1f))
            QuickActionCard(Icons.Outlined.Search, "Browse", onViewAllGroups, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StudyGroupSection(title: String, groups: List<Group>, emptyMessage: String, onGroupClick: (String) -> Unit, onViewAll: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold)
            TextButton(onClick = onViewAll) { Text("View all") }
        }
        if (groups.isEmpty()) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groups.take(3).forEach { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onGroupClick(group.documentId.ifEmpty { group.groupId }) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(group.title, fontWeight = FontWeight.Bold)
                            Text(group.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupStatsCard(myGroupCount: Int, maxMembersPerGroup: Int) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(myGroupCount.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("My Groups", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(maxMembersPerGroup.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Max Limit", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}