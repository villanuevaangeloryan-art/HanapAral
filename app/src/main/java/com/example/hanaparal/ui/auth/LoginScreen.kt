package com.example.hanaparal.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "HanapAral", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Cloud Based Integrated Study Group Finder", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Sign in with Google")
        }
    }
}