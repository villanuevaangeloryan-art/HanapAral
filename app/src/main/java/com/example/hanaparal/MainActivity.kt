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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.example.hanaparal.ui.profile.ProfileViewModel
import com.example.hanaparal.ui.settings.SuperuserScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.util.ConfigManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Google Sign-In Setup
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
                    val profileViewModel: ProfileViewModel = viewModel()
                    val navController = rememberNavController()
                    val user by authViewModel.userState.collectAsState()

                    // Initialize Config Manager
                    val configManager = remember { ConfigManager() }

                    // Get values from Remote Config
                    val isCreateGroupEnabled by configManager.isCreationEnabled.collectAsState()
                    val maxMembers by configManager.maxMembers.collectAsState()
                    val announcement by configManager.announcement.collectAsState()
                    val isMaxEditable by configManager.isMaxMemberEditable.collectAsState()

                    // Loading states for checking profile
                    var isLoadingProfile by remember { mutableStateOf(true) }
                    var hasProfile by remember { mutableStateOf(false) }

                    // Check if the student already has a profile in Firestore
                    LaunchedEffect(user) {
                        val currentUser = user
                        if (currentUser != null) {
                            profileViewModel.loadProfile(currentUser.uid) { student ->
                                hasProfile = student != null && student.name.isNotBlank()
                                isLoadingProfile = false
                            }
                        } else {
                            isLoadingProfile = false
                        }
                    }

                    // Notification Permission Launcher
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        Log.d("MainActivity", "Permission: $isGranted")
                    }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) Log.d("MainActivity", "Token: ${task.result}")
                        }
                    }

                    // Google Login Launcher
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                            authViewModel.signInWithGoogle(credential) {
                                // Re-check if profile exists after logging in
                                val db = Firebase.firestore
                                Firebase.auth.currentUser?.uid?.let { uid ->
                                    db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                                        if (doc.exists() && !doc.getString("name").isNullOrBlank()) {
                                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                        } else {
                                            navController.navigate("profile") { popUpTo("login") { inclusive = true } }
                                        }
                                    }
                                }
                            }
                        } catch (e: ApiException) {
                            Toast.makeText(this@MainActivity, "Sign-in Failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (isLoadingProfile) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Choose starting screen
                        val startDest = when {
                            user == null -> "login"
                            hasProfile -> "home"
                            else -> "profile"
                        }

                        NavHost(navController = navController, startDestination = startDest) {
                            // User Authentication
                            composable("login") {
                                LoginScreen(onSignInClick = { launcher.launch(googleSignInClient.signInIntent) })
                            }

                            // Student Profile Management
                            composable("profile") {
                                user?.let { currentUser ->
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

                            // Dashboard
                            composable("home") {
                                val currentUser = user
                                val homeViewModel: HomeViewModel = viewModel()
                                val myGroups by homeViewModel.myGroups.collectAsState()

                                LaunchedEffect(currentUser?.uid) {
                                    currentUser?.uid?.let { homeViewModel.loadMyGroups(it) }
                                    configManager.fetchConfigs()
                                }

                                HomeScreen(
                                    userName = currentUser?.displayName ?: "Student",
                                    userInitial = currentUser?.displayName?.take(1)?.uppercase() ?: "S",
                                    myGroups = myGroups,
                                    isCreateGroupEnabled = isCreateGroupEnabled,
                                    maxMembersPerGroup = maxMembers.toInt(),
                                    announcementHeader = announcement,
                                    onUnlockClick = { navController.navigate("superuser") },
                                    onSignOut = {
                                        authViewModel.signOut()
                                        googleSignInClient.signOut()
                                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                    },
                                    onCreateGroup = { navController.navigate("create_group") },
                                    onViewAllGroups = { navController.navigate("group_list") },
                                    onSettingsClick = { navController.navigate("superuser") }
                                )
                            }

                            // Join Study Groups
                            composable("group_list") {
                                user?.let { currentUser ->
                                    GroupListScreen(
                                        currentUserId = currentUser.uid,
                                        onBack = { navController.popBackStack() },
                                        onCreateGroup = { navController.navigate("create_group") }
                                    )
                                }
                            }

                            // Study Group Creation
                            composable("create_group") {
                                user?.let { currentUser ->
                                    CreateGroupScreen(
                                        currentUserId = currentUser.uid,
                                        currentUserName = currentUser.displayName ?: "",
                                        isCreationEnabled = isCreateGroupEnabled,
                                        globalMaxMembers = maxMembers.toInt(),
                                        isMaxEditable = isMaxEditable,
                                        onBack = { navController.popBackStack() },
                                        onGroupCreated = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Biometric Authentication
                            composable("superuser") {
                                SuperuserScreen(
                                    activity = this@MainActivity,
                                    onAuthenticated = {
                                        configManager.unlockCreationFeature()
                                        navController.navigate("superuser_dashboard") {
                                            popUpTo("superuser") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Remote Configuration Dashboard
                            composable("superuser_dashboard") {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Superuser Dashboard", style = MaterialTheme.typography.headlineMedium)
                                    Text("Fingerprint Verified 🔒", modifier = Modifier.padding(vertical = 16.dp))
                                    Text(
                                        "Use the Firebase Console to manage app settings and toggles.",
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 32.dp)) {
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
}