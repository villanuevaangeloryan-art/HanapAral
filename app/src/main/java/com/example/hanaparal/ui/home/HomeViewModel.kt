package com.example.hanaparal.ui.home

import androidx.lifecycle.ViewModel
import com.example.hanaparal.data.model.Group
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups: StateFlow<List<Group>> = _myGroups

    private var groupsListener: ListenerRegistration? = null
    private val db = Firebase.firestore

    fun loadMyGroups(userId: String) {
        groupsListener?.remove()

        groupsListener = db.collection("groups")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val groupsList = snapshot.documents.mapNotNull { doc ->
                    val group = doc.toObject(Group::class.java)
                    group?.copy(documentId = doc.id)
                }

                _myGroups.value = groupsList
            }
    }

    override fun onCleared() {
        groupsListener?.remove()
        groupsListener = null
        super.onCleared()
    }
}