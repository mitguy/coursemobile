package edu.corp.glitch.ui.screens.live

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.corp.glitch.data.models.Follow
import edu.corp.glitch.data.models.User
import edu.corp.glitch.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    onStreamClick: (String) -> Unit,
    onUserClick: (String) -> Unit = {},
    viewModel: LiveViewModel = hiltViewModel()
) {
    val liveFollows by viewModel.liveFollows.collectAsState()
    val usersMap by viewModel.usersMap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Follows") },
                actions = {
                    IconButton(onClick = { viewModel.loadLiveFollows() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
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
        } else if (liveFollows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No live streams from your follows")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(liveFollows) { follow ->
                    val user = follow.toStream?.username?.let { usersMap[it] }
                    LiveStreamCard(
                        follow = follow,
                        user = user,
                        onStreamClick = onStreamClick,
                        onUserClick = onUserClick
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStreamCard(
    follow: Follow,
    user: User?,
    onStreamClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { follow.toStream?.username?.let { onStreamClick(it) } }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                username = follow.toStream?.username ?: "",
                profilePicData = user?.profilePic,
                size = 56.dp,
                modifier = Modifier.clickable { 
                    follow.toStream?.username?.let { onUserClick(it) }
                }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = follow.toStream?.username ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { 
                        follow.toStream?.username?.let { onUserClick(it) }
                    }
                )
                Text(
                    text = follow.toStream?.title ?: "No title",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text("LIVE")
                    }
                    Text("${follow.toStream?.viewers ?: 0} viewers")
                }
            }
        }
    }
}
