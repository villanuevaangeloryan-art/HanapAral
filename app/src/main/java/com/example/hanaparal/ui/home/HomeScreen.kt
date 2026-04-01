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
import androidx.compose.ui.tooling.preview.Preview
import com.example.hanaparal.data.model.StudyGroupItem

@Composable
fun HomeScreen(
    userName: String = "Student",
    userCourse: String = "",
    userInitial: String = "S",
    onCreateGroup: () -> Unit = {},
    onViewAllGroups: () -> Unit = {},
    onGroupClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettingsClick: () -> Unit = {}, // <-- ADDED: Parameter for Navigation
    myGroups: List<StudyGroupItem> = emptyList(),
    suggestedGroups: List<StudyGroupItem> = emptyList(),
    isCreateGroupEnabled: Boolean = true,
    maxMembersPerGroup: Int = 10,
    announcementHeader: String = "Welcome to HanapAral"
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                userName = userName,
                userInitial = userInitial,
                onNotificationClick = onNotificationClick,
                onSignOut = onSignOut,
                onSettingsClick = onSettingsClick // <-- ADDED: Passing to TopBar
            )
        },
        floatingActionButton = {
            if (isCreateGroupEnabled) {
                ExtendedFloatingActionButton(
                    onClick = onCreateGroup,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                    },
                    text = {
                        Text(
                            text = "Create Group",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
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
            AnnouncementBanner(message = announcementHeader)
            WelcomeCard(name = userName, course = userCourse)
            QuickActionsSection(
                isCreateGroupEnabled = isCreateGroupEnabled,
                onCreateGroup = onCreateGroup,
                onViewAllGroups = onViewAllGroups
            )
            StudyGroupSection(
                title = "My Study Groups",
                groups = myGroups,
                emptyMessage = "You have not joined any groups yet.",
                onGroupClick = onGroupClick,
                onViewAll = onViewAllGroups
            )
            StudyGroupSection(
                title = "Suggested Groups",
                groups = suggestedGroups,
                emptyMessage = "No suggested groups available.",
                onGroupClick = onGroupClick,
                onViewAll = onViewAllGroups
            )
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
    onSettingsClick: () -> Unit // <-- ADDED
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
            // <-- ADDED: Superuser / Settings Gear Icon
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Superuser Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onNotificationClick) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onSignOut) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitial,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun AnnouncementBanner(message: String) {
    if (message.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WelcomeCard(name: String, course: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Good day, ${name.split(" ").firstOrNull() ?: name}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            if (course.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = course,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Find and join study groups that match your course and interests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    isCreateGroupEnabled: Boolean,
    onCreateGroup: () -> Unit,
    onViewAllGroups: () -> Unit
) {
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.GroupAdd,
            label = "Create Group",
            enabled = isCreateGroupEnabled,
            onClick = onCreateGroup
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Search,
            label = "Browse Groups",
            enabled = true,
            onClick = onViewAllGroups
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.CalendarToday,
            label = "Schedule",
            enabled = true,
            onClick = { /* teammate adds logic */ }
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StudyGroupSection(
    title: String,
    groups: List<StudyGroupItem>,
    emptyMessage: String,
    onGroupClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onViewAll) {
            Text(
                text = "View all",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (groups.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            groups.forEach { group ->
                StudyGroupCard(
                    group = group,
                    onClick = { onGroupClick(group.id) }
                )
            }
        }
    }
}

@Composable
private fun StudyGroupCard(
    group: StudyGroupItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = group.course,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${group.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (group.isAdmin) {
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Admin",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupStatsCard(
    myGroupCount: Int,
    maxMembersPerGroup: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                value = myGroupCount.toString(),
                label = "My Groups"
            )
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline
            )
            StatItem(
                value = maxMembersPerGroup.toString(),
                label = "Max per Group"
            )
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.outline
            )
            StatItem(
                value = "Active",
                label = "Status"
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            userName = "John Dela Cruz",
            userCourse = "BS Information Technology",
            userInitial = "J",
            myGroups = listOf(
                StudyGroupItem(
                    id = "1",
                    name = "Android Development",
                    course = "BS Computer Science",
                    memberCount = 8,
                    isAdmin = true
                ),
                StudyGroupItem(
                    id = "2",
                    name = "Database Systems",
                    course = "BS Information Technology",
                    memberCount = 12,
                    isAdmin = false
                )
            ),
            suggestedGroups = listOf(
                StudyGroupItem(
                    id = "3",
                    name = "Machine Learning",
                    course = "BS Data Science",
                    memberCount = 15,
                    isAdmin = false
                ),
                StudyGroupItem(
                    id = "4",
                    name = "Web Development",
                    course = "BS Computer Science",
                    memberCount = 10,
                    isAdmin = false
                )
            ),
            isCreateGroupEnabled = true,
            maxMembersPerGroup = 10,
            announcementHeader = "Welcome back! New study groups available."
        )
    }
}