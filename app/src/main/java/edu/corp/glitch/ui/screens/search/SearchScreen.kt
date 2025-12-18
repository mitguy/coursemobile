package edu.corp.glitch.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.corp.glitch.data.models.Stream
import edu.corp.glitch.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onStreamClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val streams by viewModel.streams.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchedUsers by viewModel.searchedUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var query by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.searchUser(it)
                        },
                        placeholder = { Text("Search users...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp), // Add right padding
                        trailingIcon = {
                            Icon(Icons.Default.Search, "Search")
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
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
        } else if (searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (searchedUsers.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users found matching '$searchQuery'")
                        }
                    }
                } else {
                    items(searchedUsers) { user ->
                        UserSearchResultItem(user = user, onClick = { onUserClick(user.username) })
                    }
                }
            }
        } else {
            // Show all live streams
            if (streams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No live streams")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(streams) { stream ->
                        StreamCard(stream, onStreamClick)
                    }
                }
            }
        }
    }
}

@Composable
fun StreamCard(stream: Stream, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(stream.username) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stream.username,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stream.title ?: "No title",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (stream.live) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text("LIVE")
                    }
                }
                Text("${stream.viewers} viewers")
            }
        }
    }
}

@Composable
fun UserSearchResultItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.username, style = MaterialTheme.typography.titleMedium)
            if (!user.bio.isNullOrBlank()) {
                Text(text = user.bio, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}
