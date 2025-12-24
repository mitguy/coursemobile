package edu.corp.glitch.ui.screens.stream

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import edu.corp.glitch.data.models.ChatMessage
import edu.corp.glitch.ui.components.ShowErrorSnackbar
import edu.corp.glitch.ui.components.UserAvatar
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    username: String,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    viewModel: StreamViewModel = hiltViewModel()
) {
    val stream by viewModel.stream.collectAsState()
    val user by viewModel.user.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isOwnStream by viewModel.isOwnStream.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var isFullscreen by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(username) {
        viewModel.loadStream(username)
        viewModel.loadUser(username)
        viewModel.checkIsOwnStream(username)
        viewModel.connectToChat(username)
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    LaunchedEffect(isFullscreen) {
        onFullscreenChange(isFullscreen)
        activity?.let {
            if (isFullscreen) {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val window = it.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val window = it.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // viewModel.disconnectChat()
            onFullscreenChange(false)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.let {
                val window = it.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    ShowErrorSnackbar(
        error = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = { viewModel.clearError() }
    )

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayer(
                streamUrl = "http://10.87.7.197:8080/hls/$username.m3u8",
                // streamUrl = "http://arch.local:8080/hls/$username.m3u8",
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { isFullscreen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Stream") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        if (isOwnStream) {
                            IconButton(onClick = { showEditTitleDialog = true }) {
                                Icon(Icons.Default.Edit, "Edit Stream Title")
                            }
                        }
                    }
                )
            },
            // bottomBar = { if (!isFullscreen) BottomAppBar {} },
            snackbarHost = { SnackbarHost(snackbarHostState) }
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black)
                    ) {
                        if (stream?.live == true) {
                            VideoPlayer(
                                streamUrl = "http://10.87.7.197:8080/hls/$username.m3u8",
                                // streamUrl = "http://arch.local:8080/hls/$username.m3u8",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Stream is offline",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        if (stream?.live == true) {
                            IconButton(
                                onClick = { isFullscreen = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }

                        if (stream?.live == true) {
                            Badge(
                                containerColor = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text("LIVE", color = Color.White)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stream?.title ?: "No title",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${stream?.viewers ?: 0}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { onUserClick(username) }
                            ) {
                                UserAvatar(
                                    username = user?.username ?: username,
                                    profilePicData = user?.profilePic,
                                    size = 32.dp
                                )
                                Column {
                                    Text(
                                        text = user?.username ?: username,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${user?.followersCount ?: 0} followers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    user?.id?.let { viewModel.toggleFollow(it) }
                                },
                                colors = if (isFollowing) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    ButtonDefaults.buttonColors()
                                }
                            ) {
                                Text(if (isFollowing) "Following" else "Follow")
                            }
                        }
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text(
                            text = "Chat",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { message ->
                                ChatMessageItem(message)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text("Send a message") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText)
                                        messageText = ""
                                    }
                                },
                                enabled = messageText.isNotBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Send")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditTitleDialog) {
        EditStreamTitleDialog(
            currentTitle = stream?.title ?: "",
            onDismiss = { showEditTitleDialog = false },
            onSave = { newTitle ->
                viewModel.updateStreamTitle(newTitle)
                showEditTitleDialog = false
            }
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    streamUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = message.username,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = formatTimestamp(message.at),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        formatter.format(Date.from(instant))
    } catch (e: Exception) {
        timestamp
    }
}

@Composable
fun EditStreamTitleDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stream Title") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Stream Title") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title)
                    }
                },
                enabled = title.isNotBlank()
            ) {
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
