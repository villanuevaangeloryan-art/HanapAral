package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.Group
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

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

    fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {
        val ref = db.collection("groups").document()
        val newGroup = group.copy(groupId = ref.id, documentId = ref.id)
        
        ref.set(newGroup)
            .addOnSuccessListener {
                // Subscribe creator to group topic
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
                // Subscribe user to group topic for notifications
                subscribeToGroup(groupDocumentId)
                
                // Send notification to others in the group
                sendGroupNotification(
                    groupId = groupDocumentId,
                    title = "New Member!",
                    message = "$userName has joined the group."
                )
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

    /**
     * Sends a notification to all members of a group via FCM topic.
     * Note: In a real production app, this should be done via a backend or Cloud Functions
     * for security reasons. For this implementation, we are assuming the FCM server key
     * is handled or we use a simulated approach if direct client-to-topic sending is restricted.
     */
    fun sendGroupNotification(groupId: String, title: String, message: String) {
        // Topic-based notification
        // This is a placeholder for triggering the notification. 
        // Typically requires calling the FCM REST API or a Cloud Function.
        
        // For demonstration, we'll log it. 
        // Integration with a simple HTTP call to FCM would go here if server key were provided.
    }

    fun sendAnnouncement(groupId: String, adminName: String, content: String) {
        sendGroupNotification(
            groupId = groupId,
            title = "Announcement from $adminName",
            message = content
        )
    }

    fun sendStudyReminder(groupId: String, subject: String, time: String) {
        sendGroupNotification(
            groupId = groupId,
            title = "Study Reminder: $subject",
            message = "Reminder for our session at $time"
        )
    }
}
