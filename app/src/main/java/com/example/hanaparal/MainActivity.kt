package com.example.hanaparal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
import com.example.hanaparal.ui.settings.SuperuserScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.util.ConfigManager // <-- IMPORTED YOUR CONFIG MANAGER
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : FragmentActivity() {
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

                    // --- INITIALIZE YOUR CONFIG MANAGER ---
                    val configManager = remember { ConfigManager() }

                    // Collect the values so the UI updates automatically!
                    val isCreateGroupEnabled by configManager.isCreationEnabled.collectAsState()
                    val maxMembers by configManager.maxMembers.collectAsState()
                    val announcement by configManager.announcement.collectAsState()

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            Log.d("MainActivity", "Notification permission granted")
                        } else {
                            Log.d("MainActivity", "Notification permission denied")
                        }
                    }

                    LaunchedEffect(Unit) {
                        // Notifications Permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }

                        // FCM Token
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                                return@addOnCompleteListener
                            }
                            Log.d("MainActivity", "FCM Token: ${task.result}")
                        }
                    }

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
                            Toast.makeText(this@MainActivity, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val startDest = if (user == null) "login" else "profile"

                    NavHost(
                        navController = navController,
                        startDestination = startDest
                    ) {
                        composable("login") {
                            LoginScreen(
                                onSignInClick = { launcher.launch(googleSignInClient.signInIntent) }
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
                                // Refresh configs when user arrives at Home
                                configManager.fetchConfigs()
                            }

                            HomeScreen(
                                userName = currentUser?.displayName ?: "Student",
                                userCourse = "",
                                userInitial = currentUser?.displayName?.take(1)?.uppercase() ?: "S",
                                myGroups = myGroups,

                                // --- PASSING CONFIG MANAGER DATA TO UI ---
                                isCreateGroupEnabled = isCreateGroupEnabled,
                                maxMembersPerGroup = maxMembers.toInt(),
                                announcementHeader = announcement,

                                onSignOut = {
                                    authViewModel.signOut()
                                    googleSignInClient.signOut()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onCreateGroup = { navController.navigate("create_group") },
                                onViewAllGroups = { navController.navigate("group_list") },
                                onGroupClick = { _ -> },
                                onNotificationClick = { },
                                onSettingsClick = { navController.navigate("superuser") }
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

                        // --- MEMBER 6: THE BIOMETRIC GATEKEEPER ---
                        composable("superuser") {
                            SuperuserScreen(
                                activity = this@MainActivity,
                                onAuthenticated = {
                                    navController.navigate("superuser_dashboard") {
                                        popUpTo("superuser") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- MEMBER 6: THE SECURE DASHBOARD ---
                        composable("superuser_dashboard") {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Superuser Dashboard", style = MaterialTheme.typography.headlineMedium)
                                Text("Fingerprint Verified 🔒", modifier = Modifier.padding(vertical = 16.dp))
                                Text(
                                    "To toggle features, update strings, and manage state, log in to your Firebase Console and update the Remote Config parameters.",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.padding(top = 32.dp)
                                ) {
                                    Text("Back to Home")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}