package com.example.hanaparal.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    //tracks the current user state for the entire app
    private val _userState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val userState: StateFlow<FirebaseUser?> = _userState

    fun signInWithGoogle(credential: AuthCredential, onSuccess: () -> Unit) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userState.value = auth.currentUser
                onSuccess()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _userState.value = null
    }
}