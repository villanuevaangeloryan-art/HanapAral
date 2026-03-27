package com.example.hanaparal.ui.profile

import androidx.lifecycle.ViewModel
import com.example.hanaparal.data.model.Student
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ProfileViewModel : ViewModel() {
    private val db = Firebase.firestore

    fun saveProfile(student: Student, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(student.uid)
            .set(student)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}