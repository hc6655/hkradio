package com.example.hkradio

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hkradio.ui.theme.HKRadioTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HKRadioTheme(darkTheme = true) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScaffoldScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldScreen() {
    var selected by remember { mutableStateOf<ChannelData?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    val activity = (LocalContext.current as? Activity)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = selected) {
        if (selected != null) {
            isLoading = true
            isPlaying = false
            showLoading = true
            Player.play(selected!!.link)
            showLoading = false
            isPlaying = true
            isLoading = false
        }
    }

    LaunchedEffect(key1 = isPlaying) {
        if (selected != null) {
            if (!isPlaying)
                Player.pause()
            else
                Player.resume()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HK Radio") }
            )
        },
        bottomBar = {
            if (selected != null) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(70.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.DarkGray
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selected!!.name,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            textAlign = TextAlign.Center
                        )

                        if (showLoading)
                            CircularProgressIndicator()

                        val image = if (isPlaying)
                            Icons.Filled.Pause
                        else
                            Icons.Filled.PlayArrow

                        IconButton(
                            onClick = {
                                if (!isLoading)
                                    isPlaying = !isPlaying
                            },
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(50.dp)
                            )
                        }


                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                items(ChannelData.data) { channel ->
                    ListItem(
                        headlineText = { Text(
                            text = channel.name,
                            fontSize = 30.sp
                        ) },
                        modifier = Modifier.clickable {
                            selected = channel
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selected == channel)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent
                        )
                    )
                    Divider()
                }
            }
        }
    }

    BackHandler(enabled = true) {
        if (snackbarHostState.currentSnackbarData == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("再按一次即可離開")
            }
        } else {
            isPlaying = false
            Player.stop()
            activity?.finish()
        }
    }
}