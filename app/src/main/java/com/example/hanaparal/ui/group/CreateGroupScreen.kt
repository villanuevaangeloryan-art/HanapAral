package com.example.hanaparal.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.model.Group
import com.example.hanaparal.data.repository.GroupRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    currentUserId: String,
    currentUserName: String,
    isCreationEnabled: Boolean,
    globalMaxMembers: Int,
    isMaxEditable: Boolean,
    onBack: () -> Unit,
    onGroupCreated: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var maxMembersInput by remember { mutableStateOf(globalMaxMembers.toString()) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Study Group", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (!isCreationEnabled) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Lock, null, Modifier.size(64.dp), MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Text("Group creation is disabled.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Group Information", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = groupName, onValueChange = { groupName = it }, label = { Text("Group Name") },
                    leadingIcon = { Icon(Icons.Outlined.Groups, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = course, onValueChange = { course = it }, label = { Text("Course / Subject") },
                    leadingIcon = { Icon(Icons.Outlined.School, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = maxMembersInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) { val v = it.toIntOrNull() ?: 0; if (v <= globalMaxMembers) maxMembersInput = it } },
                    label = { Text("Max Members") }, leadingIcon = { Icon(Icons.Outlined.Numbers, null) },
                    modifier = Modifier.fillMaxWidth(), enabled = isMaxEditable, shape = RoundedCornerShape(14.dp),
                    supportingText = { Text("Cloud Limit: $globalMaxMembers") }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        isLoading = true
                        val group = Group(groupId = "", documentId = "", title = groupName, subject = course, description = "", adminId = currentUserId, adminName = currentUserName, members = listOf(currentUserId), maxMembers = maxMembersInput.toIntOrNull() ?: globalMaxMembers, isOpen = true)
                        GroupRepository.createGroup(group) { success, _ -> isLoading = false; if (success) onGroupCreated() }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    enabled = groupName.isNotBlank() && course.isNotBlank() && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Create Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}