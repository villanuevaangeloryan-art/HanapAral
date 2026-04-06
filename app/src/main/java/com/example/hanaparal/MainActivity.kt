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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hanaparal.data.model.Group
import com.example.hanaparal.ui.auth.AuthViewModel
import com.example.hanaparal.ui.auth.LoginScreen
import com.example.hanaparal.ui.group.CreateGroupScreen
import com.example.hanaparal.ui.group.GroupDetailsScreen
import com.example.hanaparal.ui.group.GroupListScreen
import com.example.hanaparal.ui.home.HomeScreen
import com.example.hanaparal.ui.home.HomeViewModel
import com.example.hanaparal.ui.profile.ProfileScreen
import com.example.hanaparal.ui.profile.ProfileViewModel
import com.example.hanaparal.ui.settings.SuperuserDashboardScreen // <-- FIXED: Added this import!
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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            HanapAralTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val authViewModel: AuthViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()
                    val navController = rememberNavController()
                    val user by authViewModel.userState.collectAsState()
                    val configManager = remember { ConfigManager() }

                    val isCreateGroupEnabled by configManager.isCreationEnabled.collectAsState()
                    val maxMembers by configManager.maxMembers.collectAsState()
                    val announcement by configManager.announcement.collectAsState()
                    val isMaxEditable by configManager.isMaxMemberEditable.collectAsState() // <-- FIXED: Now it exists again

                    var isLoadingProfile by remember { mutableStateOf(true) }
                    var hasProfile by remember { mutableStateOf(false) }

                    LaunchedEffect(user) {
                        if (user != null) {
                            profileViewModel.loadProfile(user!!.uid) { student ->
                                hasProfile = student != null && student.name.isNotBlank()
                                isLoadingProfile = false
                            }
                        } else { isLoadingProfile = false }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) Log.d("FCM", "Token: ${task.result}")
                        }
                    }

                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            authViewModel.signInWithGoogle(GoogleAuthProvider.getCredential(account.idToken, null)) {
                                Firebase.auth.currentUser?.uid?.let { uid ->
                                    Firebase.firestore.collection("users").document(uid).get().addOnSuccessListener { doc ->
                                        if (doc.exists() && !doc.getString("name").isNullOrBlank()) {
                                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                        } else {
                                            navController.navigate("profile") { popUpTo("login") { inclusive = true } }
                                        }
                                    }
                                }
                            }
                        } catch (_: ApiException) { Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_SHORT).show() }
                    }

                    if (isLoadingProfile) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        NavHost(navController = navController, startDestination = if (user == null) "login" else if (hasProfile) "home" else "profile") {
                            composable("login") { LoginScreen(onSignInClick = { launcher.launch(googleSignInClient.signInIntent) }) }

                            composable("profile") {
                                ProfileScreen(uid = user?.uid ?: "", email = user?.email ?: "", onProfileSaved = { navController.navigate("home") { popUpTo("profile") { inclusive = true } } })
                            }

                            composable("home") {
                                val homeViewModel: HomeViewModel = viewModel()
                                val myGroups by homeViewModel.myGroups.collectAsState()
                                LaunchedEffect(user?.uid) { user?.uid?.let { homeViewModel.loadMyGroups(it) }; configManager.fetchConfigs() }

                                HomeScreen(
                                    userName = user?.displayName ?: "Student",
                                    userInitial = user?.displayName?.take(1)?.uppercase() ?: "S",
                                    myGroups = myGroups,
                                    isCreateGroupEnabled = isCreateGroupEnabled,
                                    maxMembersPerGroup = maxMembers.toInt(),
                                    announcementHeader = announcement,
                                    onUnlockClick = { navController.navigate("superuser") },
                                    onSignOut = { authViewModel.signOut(); googleSignInClient.signOut(); navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                                    onCreateGroup = { navController.navigate("create_group") },
                                    onViewAllGroups = { navController.navigate("group_list") },
                                    onGroupClick = { groupId -> navController.navigate("group_details/$groupId") },
                                    onSettingsClick = { navController.navigate("superuser") }
                                )
                            }

                            composable("group_list") {
                                GroupListScreen(
                                    currentUserId = user?.uid ?: "",
                                    onBack = { navController.popBackStack() },
                                    onCreateGroup = { navController.navigate("create_group") },
                                    onGroupClick = { groupId -> navController.navigate("group_details/$groupId") }
                                )
                            }

                            composable("create_group") {
                                CreateGroupScreen(
                                    currentUserId = user?.uid ?: "",
                                    currentUserName = user?.displayName ?: "",
                                    isCreationEnabled = isCreateGroupEnabled,
                                    globalMaxMembers = maxMembers.toInt(),
                                    isMaxEditable = isMaxEditable,
                                    onBack = { navController.popBackStack() },
                                    onGroupCreated = { navController.popBackStack() }
                                )
                            }

                            composable("group_details/{groupId}") { backStackEntry ->
                                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                                var groupData by remember { mutableStateOf<Group?>(null) }

                                LaunchedEffect(groupId) {
                                    Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { doc ->
                                        groupData = doc.toObject(Group::class.java)?.copy(documentId = doc.id)
                                    }
                                }

                                if (groupData != null) {
                                    GroupDetailsScreen(
                                        group = groupData!!,
                                        currentUserId = user?.uid ?: "",
                                        currentUserName = user?.displayName ?: "Student",
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                }
                            }

                            // BIOMETRIC AND DASHBOARD ROUTES
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

                            composable("superuser_dashboard") {
                                SuperuserDashboardScreen(
                                    isCreationEnabled = isCreateGroupEnabled,
                                    maxMembers = maxMembers,
                                    announcement = announcement, // <-- FIXED: Was incorrectly typed as 'announcementHeader'
                                    onSyncConfigs = { configManager.fetchConfigs() },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}