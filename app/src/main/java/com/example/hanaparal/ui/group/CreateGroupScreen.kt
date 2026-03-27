package com.example.hanaparal.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.hanaparal.data.model.Group
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun CreateGroupScreen(currentUserId: String, isCreationEnabled: Boolean) {
    var title by remember { mutableStateOf("") }
    val db = Firebase.firestore

    if (!isCreationEnabled) return // Listens to Member 6's Remote Config

    Column {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Group Title") })
        Button(onClick = {
            val ref = db.collection("groups").document()
            val group = Group(ref.id, title, currentUserId, listOf(currentUserId))
            ref.set(group)
        }) {
            Text("Create Group")
        }
    }
}