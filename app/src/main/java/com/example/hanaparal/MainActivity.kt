package com.example.hanaparal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.ui.auth.AuthViewModel
import com.example.hanaparal.ui.auth.LoginScreen
import com.example.hanaparal.ui.group.CreateGroupScreen
import com.example.hanaparal.ui.group.GroupListScreen
import com.example.hanaparal.ui.home.HomeScreen
import com.example.hanaparal.ui.home.HomeViewModel
import com.example.hanaparal.ui.profile.ProfileScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            HanapAralTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val navController = rememberNavController()
                    val user by authViewModel.userState.collectAsState()

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                            authViewModel.signInWithGoogle(credential) {
                                navController.navigate("profile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        } catch (e: ApiException) {
                            Toast.makeText(
                                this@MainActivity,
                                "Login Failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    val startDest = if (user == null) "login" else "profile"

                    NavHost(
                        navController = navController,
                        startDestination = startDest
                    ) {
                        composable("login") {
                            LoginScreen(
                                onSignInClick = {
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            )
                        }

                        composable("profile") {
                            val currentUser = user
                            if (currentUser != null) {
                                ProfileScreen(
                                    uid = currentUser.uid,
                                    email = currentUser.email ?: "",
                                    onProfileSaved = {
                                        navController.navigate("home") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable("home") {
                            val currentUser = user
                            val homeViewModel: HomeViewModel = viewModel()
                            val myGroups by homeViewModel.myGroups.collectAsState()

                            LaunchedEffect(currentUser?.uid) {
                                currentUser?.uid?.let { homeViewModel.loadMyGroups(it) }
                            }

                            HomeScreen(
                                userName = currentUser?.displayName ?: "Student",
                                userCourse = "",
                                userInitial = currentUser?.displayName?.take(1)?.uppercase() ?: "S",
                                myGroups = myGroups,
                                onSignOut = {
                                    authViewModel.signOut()
                                    googleSignInClient.signOut()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onCreateGroup = { navController.navigate("create_group") },
                                onViewAllGroups = {
                                    navController.navigate("group_list")
                                },
                                onGroupClick = { _ -> /* teammate: navigate to group detail */ },
                                onNotificationClick = { /* teammate: show notifications */ }
                            )
                        }

                        composable("group_list") {
                            val currentUser = user
                            if (currentUser != null) {
                                GroupListScreen(
                                    currentUserId = currentUser.uid,
                                    onBack = { navController.popBackStack() },
                                    onCreateGroup = { navController.navigate("create_group") }
                                )
                            }
                        }

                        composable("create_group") {
                            val currentUser = user
                            if (currentUser != null) {
                                CreateGroupScreen(
                                    currentUserId = currentUser.uid,
                                    currentUserName = currentUser.displayName ?: "",
                                    onBack = { navController.popBackStack() },
                                    onGroupCreated = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
