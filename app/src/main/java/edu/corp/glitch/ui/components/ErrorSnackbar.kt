package edu.corp.glitch.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun ShowErrorSnackbar(
    error: String?,
    snackbarHostState: SnackbarHostState,
    onErrorShown: () -> Unit
) {
    LaunchedEffect(error) {
        error?.let {
            launch {
                snackbarHostState.showSnackbar(it)
                onErrorShown()
            }
        }
    }
}

