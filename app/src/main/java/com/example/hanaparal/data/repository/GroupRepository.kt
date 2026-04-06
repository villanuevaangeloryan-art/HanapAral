package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.Group
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

sealed class JoinGroupResult {
    object Success : JoinGroupResult()
    object AlreadyMember : JoinGroupResult()
    object GroupFull : JoinGroupResult()
    object GroupClosed : JoinGroupResult()
    object NotFound : JoinGroupResult()
    data class Failure(val message: String) : JoinGroupResult()
}

object GroupRepository {
    private val db = Firebase.firestore
    private val fcm = FirebaseMessaging.getInstance()

    fun groupFromDocument(doc: DocumentSnapshot): Group? {
        return try {
            val group = doc.toObject(Group::class.java)
            group?.copy(documentId = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllGroups(onComplete: (List<Group>) -> Unit) {
        db.collection("groups")
            .get()
            .addOnSuccessListener { snapshot ->
                val groupsList = snapshot.documents.mapNotNull { document ->
                    groupFromDocument(document)
                }
                onComplete(groupsList)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {
        val ref = db.collection("groups").document()
        val newGroup = group.copy(groupId = ref.id, documentId = ref.id)

        ref.set(newGroup)
            .addOnSuccessListener {
                subscribeToGroup(ref.id)
                onComplete(true, ref.id)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    fun joinGroup(groupDocumentId: String, userId: String, userName: String, onResult: (JoinGroupResult) -> Unit) {
        val groupRef = db.collection("groups").document(groupDocumentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(groupRef)
            if (!snapshot.exists()) return@runTransaction JoinGroupResult.NotFound

            val members = snapshot.get("members") as? List<*> ?: emptyList<Any>()
            val maxMembers = snapshot.getLong("maxMembers") ?: 20
            val isOpen = snapshot.getBoolean("isOpen") ?: true

            if (members.contains(userId)) return@runTransaction JoinGroupResult.AlreadyMember
            if (members.size >= maxMembers) return@runTransaction JoinGroupResult.GroupFull
            if (!isOpen) return@runTransaction JoinGroupResult.GroupClosed

            transaction.update(groupRef, "members", FieldValue.arrayUnion(userId))
            JoinGroupResult.Success
        }.addOnSuccessListener { result ->
            if (result is JoinGroupResult.Success) {
                subscribeToGroup(groupDocumentId)
                sendGroupNotification(groupDocumentId, "New Member!", "$userName has joined the group.")
            }
            onResult(result as JoinGroupResult)
        }.addOnFailureListener { e ->
            onResult(JoinGroupResult.Failure(e.message ?: "Unknown error"))
        }
    }

    fun subscribeToGroup(groupId: String) {
        fcm.subscribeToTopic("group_$groupId")
    }

    fun unsubscribeFromGroup(groupId: String) {
        fcm.unsubscribeFromTopic("group_$groupId")
    }

    fun sendGroupNotification(groupId: String, title: String, message: String) {
        // Placeholder for FCM Cloud Function trigger
    }

    fun sendAnnouncement(groupId: String, adminName: String, content: String) {
        sendGroupNotification(groupId, "Announcement from $adminName", content)
    }

    fun sendStudyReminder(groupId: String, subject: String, time: String) {
        sendGroupNotification(groupId, "Study Reminder: $subject", "Reminder for our session at $time")
    }
}