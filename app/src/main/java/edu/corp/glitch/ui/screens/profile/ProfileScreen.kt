package edu.corp.glitch.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String? = null,
    onNavigateToOwnProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var isOwnProfile by remember { mutableStateOf(false) }
    
    LaunchedEffect(username) {
        if (username != null) {
            viewModel.loadUserByUsername(username)
            isOwnProfile = viewModel.isOwnProfile()
        } else {
            viewModel.loadCurrentUser()
            isOwnProfile = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "Profile") },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = user!!.username,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = user!!.bio ?: "No bio",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Text(
                    text = "${user!!.followersCount} followers",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                if (!isOwnProfile) {
                    Button(
                        onClick = { user?.id?.let { viewModel.toggleFollow(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(if (isFollowing) "Unfollow" else "Follow")
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditBioDialog(
            currentBio = user?.bio ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newBio ->
                viewModel.updateBio(newBio)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditBioDialog(
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bio") },
        text = {
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(bio) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

