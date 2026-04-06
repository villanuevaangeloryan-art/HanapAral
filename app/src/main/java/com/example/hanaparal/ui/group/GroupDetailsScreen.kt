package com.example.hanaparal.ui.group

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.model.Group
import com.example.hanaparal.data.repository.GroupRepository
import com.example.hanaparal.data.repository.JoinGroupResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    group: Group,
    currentUserId: String,
    currentUserName: String,
    onBack: () -> Unit
) {
    var isJoining by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check if the current user is already in the members list
    val isAlreadyMember = group.members.contains(currentUserId)
    val isGroupFull = group.members.size >= group.maxMembers

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Group Information Card
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Group Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                    Text("Subject: ${group.subject}", style = MaterialTheme.typography.bodyLarge)
                    Text("Members: ${group.members.size} / ${group.maxMembers}", style = MaterialTheme.typography.bodyLarge)
                    Text("Admin: ${group.adminName}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Announcements Card
            Text("Announcements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Campaign, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("A new study session is scheduled. Please check your push notifications!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Join Button Logic
            Button(
                onClick = {
                    if (!isAlreadyMember && !isGroupFull) {
                        isJoining = true
                        GroupRepository.joinGroup(group.documentId, currentUserId, currentUserName) { result ->
                            isJoining = false
                            when (result) {
                                is JoinGroupResult.Success -> {
                                    Toast.makeText(context, "Successfully joined ${group.title}!", Toast.LENGTH_SHORT).show()
                                    onBack() // Go back to refresh the list
                                }
                                is JoinGroupResult.AlreadyMember -> Toast.makeText(context, "You are already a member.", Toast.LENGTH_SHORT).show()
                                is JoinGroupResult.GroupFull -> Toast.makeText(context, "This group is currently full.", Toast.LENGTH_SHORT).show()
                                is JoinGroupResult.GroupClosed -> Toast.makeText(context, "This group is closed.", Toast.LENGTH_SHORT).show()
                                else -> Toast.makeText(context, "Failed to join group.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                // Disable the button if joining, already joined, or full
                enabled = !isJoining && !isAlreadyMember && !isGroupFull
            ) {
                if (isJoining) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    val buttonText = when {
                        isAlreadyMember -> "Already Joined"
                        isGroupFull -> "Group Full"
                        else -> "Join Group"
                    }
                    Text(buttonText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}