package com.example.hanaparal.ui.profile

import androidx.lifecycle.ViewModel
import com.example.hanaparal.data.model.Student
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileViewModel : ViewModel() {

    private val db = Firebase.firestore

    fun saveProfile(
        uid    : String,
        name   : String,
        course : String,
        email  : String,
        onResult: (Boolean) -> Unit
    ) {
        val student = Student(uid, name, course, email)
        db.collection("users").document(uid)
            .set(student)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun loadProfile(uid: String, onLoaded: (Student?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                onLoaded(doc.toObject(Student::class.java))
            }
            .addOnFailureListener {
                onLoaded(null)
            }
    }
}
