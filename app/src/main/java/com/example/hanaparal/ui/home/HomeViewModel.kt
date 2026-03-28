package com.example.hanaparal.ui.home

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _myGroups = MutableStateFlow<List<StudyGroupItem>>(emptyList())
    val myGroups: StateFlow<List<StudyGroupItem>> = _myGroups

    fun loadMyGroups(userId: String) {
        db.collection("groups")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _myGroups.value = snapshot.documents.mapNotNull { doc ->
                        val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                        if (members.contains(userId)) {
                            StudyGroupItem(
                                id = doc.getString("groupId") ?: doc.id,
                                name = doc.getString("title") ?: "",
                                course = doc.getString("subject") ?: "",
                                memberCount = members.size,
                                isAdmin = doc.getString("adminId") == userId
                            )
                        } else null
                    }
                }
            }
    }
}
