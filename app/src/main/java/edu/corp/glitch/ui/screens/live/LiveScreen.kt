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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    onStreamClick: (String) -> Unit,
    viewModel: LiveViewModel = hiltViewModel()
) {
    val liveFollows by viewModel.liveFollows.collectAsState()
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
                    LiveStreamCard(follow, onStreamClick)
                }
            }
        }
    }
}

@Composable
fun LiveStreamCard(follow: Follow, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { follow.toStream?.username?.let { onClick(it) } }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = follow.toStream?.username ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = follow.toStream?.title ?: "No title",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Badge {
                    Text("LIVE", color = MaterialTheme.colorScheme.onError)
                }
                Text("${follow.toStream?.viewers ?: 0} viewers")
            }
        }
    }
}
