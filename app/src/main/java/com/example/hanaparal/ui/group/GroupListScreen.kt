package com.example.hanaparal.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.data.model.Group
import com.example.hanaparal.data.repository.GroupRepository
import com.example.hanaparal.data.repository.JoinGroupResult
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    currentUserId: String,
    onBack: () -> Unit,
    onCreateGroup: () -> Unit = {}
) {
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var joiningDocId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val db = Firebase.firestore

    DisposableEffect(Unit) {
        val registration = db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    groups = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    groups = snapshot.documents.mapNotNull { GroupRepository.groupFromDocument(it) }
                }
            }
        onDispose { registration.remove() }
    }

    val myGroups = remember(groups, currentUserId) {
        groups.filter { it.members.contains(currentUserId) }
    }
    val availableToJoin = remember(groups, currentUserId) {
        groups.filter { g ->
            g.isOpen &&
                !g.members.contains(currentUserId) &&
                g.members.size < g.maxMembers
        }
    }
    val allGroups = groups

    val displayList = when (selectedTab) {
        0 -> myGroups
        1 -> availableToJoin
        else -> allGroups
    }.filter {
        searchQuery.isBlank() ||
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subject.contains(searchQuery, ignoreCase = true)
    }

    fun onJoinGroup(documentId: String) {
        joiningDocId = documentId
        GroupRepository.joinGroup(
            groupDocumentId = documentId,
            userId = currentUserId,
            userName = "" // teammate will provide if needed
        ) { result ->
            joiningDocId = null
            val message = when (result) {
                JoinGroupResult.Success -> "Joined successfully"
                JoinGroupResult.AlreadyMember -> "You're already in this group"
                JoinGroupResult.GroupFull -> "This group is full"
                JoinGroupResult.GroupClosed -> "This group is closed to new members"
                JoinGroupResult.NotFound -> "Group no longer exists"
                is JoinGroupResult.Failure -> result.message
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Study Groups", fontWeight = FontWeight.Bold)
                        Text(
                            when (selectedTab) {
                                0 -> "${myGroups.size} joined"
                                1 -> "${availableToJoin.size} open to join"
                                else -> "${allGroups.size} total"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateGroup,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Group") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or subject...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Groups (${myGroups.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Join (${availableToJoin.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All (${allGroups.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (displayList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when (selectedTab) {
                                0 -> "You haven't joined any groups yet."
                                1 -> "No groups available to join right now."
                                else -> "No groups found."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedTab == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { selectedTab = 1 }) {
                                Text("Browse groups to join")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayList, key = { it.documentId.ifBlank { it.groupId } }) { group ->
                        val docId = group.documentId.ifBlank { group.groupId }
                        GroupCard(
                            group = group,
                            currentUserId = currentUserId,
                            showJoinAction = selectedTab == 1 || selectedTab == 2,
                            isJoining = joiningDocId == docId,
                            onJoin = { onJoinGroup(docId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    currentUserId: String,
    showJoinAction: Boolean = true,
    isJoining: Boolean = false,
    onJoin: () -> Unit = {}
) {
    val initial = group.title.take(1).uppercase()
    val isMember = group.members.contains(currentUserId)
    val isAdmin = group.adminId == currentUserId
    val canJoin = showJoinAction &&
        !isMember &&
        group.isOpen &&
        group.members.size < group.maxMembers

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(group.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Admin",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text(group.subject, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${group.members.size}/${group.maxMembers}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "• ${group.adminName.ifBlank { "Admin" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (group.description.isNotBlank()) {
                    Text(
                        group.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when {
                isMember -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Joined",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                canJoin -> {
                    Button(
                        onClick = onJoin,
                        enabled = !isJoining,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Join", fontSize = 12.sp)
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                            Text(
                                if (!group.isOpen) "Closed" else "Full",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
