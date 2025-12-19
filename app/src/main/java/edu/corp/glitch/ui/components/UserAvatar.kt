package edu.corp.glitch.ui.components

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    username: String,
    profilePicData: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val imageBytes = remember(profilePicData) {
        if (!profilePicData.isNullOrEmpty()) {
            try {
                Base64.decode(profilePicData, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            AsyncImage(
                model = imageBytes,
                contentDescription = "Profile picture of $username",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "Default avatar",
                modifier = Modifier.size(size * 0.6f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
