package com.example.gracanicaradio

import RadioService
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gracanicaradio.ui.theme.GracanicaRadioTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // ViewModel reference
    private val radioViewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GracanicaRadioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RadioPlayer(
                        modifier = Modifier.padding(innerPadding),
                        isPlaying = radioViewModel.isPlaying,
                        isPrepared = radioViewModel.isPrepared,
                        isBuffering = radioViewModel.isBuffering, // Pass isBuffering here
                        onPlay = {
                            radioViewModel.playStream()
                            startRadioService() // Start the foreground service when playing
                        },
                        onPause = {
                            radioViewModel.pauseStream()
                            stopRadioService() // Stop the foreground service when pausing
                        },
                        onRefresh = { restartApp() } // New onRefresh action
                    )
                }
            }
        }
    }

    // Function to start the radio service
    private fun startRadioService() {
        val intent = Intent(this, RadioService::class.java)
        startService(intent) // Start the service to play the radio stream
    }

    // Function to stop the radio service
    private fun stopRadioService() {
        val intent = Intent(this, RadioService::class.java)
        stopService(intent) // Stop the service to pause or stop the radio
    }

    // Function to restart the activity
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}


@Composable
fun RadioPlayer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isPrepared: Boolean,
    isBuffering: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRefresh: () -> Unit // Added onRefresh parameter
) {
    // State to control button visibility
    var showRefreshButton by remember { mutableStateOf(false) }

    // Timer to handle the 6-second delay
    LaunchedEffect(Unit) {
        delay(8000) // 6 seconds delay
        showRefreshButton = true
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isPortrait = maxWidth < maxHeight // Detect if in portrait or landscape

        // Background image setup
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.0f)) // Adjust alpha if needed
        )

        if (isPortrait) {
            // Portrait mode layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(2f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.radio_gracanica_logo),
                        contentDescription = "Radio Gracanica Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Show buffering state, play/pause button or refresh button
                if (isBuffering || !isPrepared) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Show refresh button only if 6 seconds have passed
                        if (showRefreshButton) {
                            Button(onClick = onRefresh) {
                                Text(text = "Refresh")
                            }
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (isPlaying) onPause() else onPlay()
                        },
                        modifier = Modifier.size(128.dp),
                        enabled = isPrepared

                    ) {
                        Image(
                            painter = painterResource(id = if (isPlaying) R.drawable.pause_button else R.drawable.play_img),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(128.dp)
                        )
                    }
                }
            }
        } else {
            // Landscape mode layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Smaller logo in landscape mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)  // 40% width of the screen for logo
                        .aspectRatio(2f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.radio_gracanica_logo),
                        contentDescription = "Radio Gracanica Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Show buffering state, play/pause button or refresh button
                if (isBuffering || !isPrepared) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Show refresh button only if 6 seconds have passed
                        if (showRefreshButton) {
                            Button(onClick = onRefresh) {
                                Text(text = "Refresh")
                            }
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (isPlaying) onPause() else onPlay()
                        },
                        modifier = Modifier.size(96.dp), // Smaller button for landscape mode
                        enabled = isPrepared
                    ) {
                        Image(
                            painter = painterResource(id = if (isPlaying) R.drawable.pause_button else R.drawable.play_img),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(96.dp) // Smaller size in landscape mode
                        )
                    }
                }
            }
        }
    }
}