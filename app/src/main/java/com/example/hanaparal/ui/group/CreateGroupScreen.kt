package com.example.hanaparal.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hanaparal.data.model.Group
import com.example.hanaparal.data.repository.GroupRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    currentUserId: String,
    currentUserName: String,
    onBack: () -> Unit,
    onGroupCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxMembersText by remember { mutableStateOf("20") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Study Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Group Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = maxMembersText,
                onValueChange = { maxMembersText = it.filter { c -> c.isDigit() } },
                label = { Text("Max Members") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Group name is required."
                        return@Button
                    }
                    if (subject.isBlank()) {
                        errorMessage = "Subject is required."
                        return@Button
                    }
                    val maxMembers = maxMembersText.toIntOrNull()?.coerceIn(2, 100) ?: 20
                    isLoading = true
                    errorMessage = null

                    val group = Group(
                        title = title.trim(),
                        subject = subject.trim(),
                        description = description.trim(),
                        adminId = currentUserId,
                        adminName = currentUserName,
                        members = listOf(currentUserId),
                        maxMembers = maxMembers,
                        isOpen = true
                    )

                    GroupRepository.createGroup(group) { success, error ->
                        isLoading = false
                        if (success) {
                            onGroupCreated()
                        } else {
                            errorMessage = "Failed to create group: $error"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Group")
                }
            }
        }
    }
}
