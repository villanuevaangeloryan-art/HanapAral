package com.example.hanaparal.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    currentUserId: String,
    currentUserName: String,
    isEditable: Boolean = true,          // <-- Receives the lock state from Firebase
    defaultMaxMembers: Int = 10,         // <-- Receives the default number from Firebase
    onBack: () -> Unit,
    onGroupCreated: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var maxMembersInput by remember { mutableStateOf(defaultMaxMembers.toString()) }

    // If the admin locks the field, force the text box to show the Firebase default number
    LaunchedEffect(isEditable, defaultMaxMembers) {
        if (!isEditable) {
            maxMembersInput = defaultMaxMembers.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Study Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Course / Subject") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = maxMembersInput,
                onValueChange = { maxMembersInput = it },
                label = { Text("Max Members") },
                enabled = isEditable, // <--- THIS IS THE FIREBASE LOCK!
                modifier = Modifier.fillMaxWidth()
            )

            // Show a warning message if the field is locked
            if (!isEditable) {
                Text(
                    text = "Max members is currently locked by the Administrator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // 1. Get the Firestore instance
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                    // 2. Prepare the data to save
                    val newGroup = hashMapOf(
                        "title" to groupName,
                        "subject" to course,
                        "maxMembers" to (maxMembersInput.toIntOrNull() ?: defaultMaxMembers),
                        "adminId" to currentUserId,
                        "adminName" to currentUserName,
                        "members" to listOf(currentUserId), // Admin is automatically the first member
                        "isOpen" to true,
                        "description" to ""
                    )

                    // 3. Save to Firebase Database
                    db.collection("groups")
                        .add(newGroup)
                        .addOnSuccessListener {
                            // Successfully saved! Now close the screen.
                            onGroupCreated()
                        }
                        .addOnFailureListener { e ->
                            // If it fails, you can log it or show a toast here
                            println("Error adding group: $e")
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                // Button is grayed out until all fields are filled
                enabled = groupName.isNotBlank() && course.isNotBlank() && maxMembersInput.isNotBlank()
            ) {
                Text("Create Group")
            }
        }
    }
}