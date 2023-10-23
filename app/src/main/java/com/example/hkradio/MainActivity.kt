package com.example.hkradio

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.hkradio.ui.theme.HKRadioTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.cast.framework.media.NotificationAction
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            HKRadioTheme(darkTheme = true) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val systemUiController = rememberSystemUiController()

                    SideEffect {
                        systemUiController.setStatusBarColor(
                            color = Color(0xff121212),
                            darkIcons = false
                        )

                        systemUiController.setNavigationBarColor(
                            color = Color(0xff121212),
                            darkIcons = false
                        )

                        systemUiController.setSystemBarsColor(
                            color = Color(0xff121212),
                            darkIcons = false
                        )
                    }

                    ScaffoldScreen()
                    //play("http://stm.rthk.hk/radio2")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                val controller = controllerFuture.get()
                Player.setController(controller)
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onDestroy() {
        Player.stop()
        MediaController.releaseFuture(controllerFuture)
        super.onDestroy()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldScreen() {
    var selected by remember { mutableStateOf<ChannelData?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = selected) {
        if (selected != null) {
            isLoading = true
            isPlaying = false
            showLoading = true
            Player.play(selected!!)
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
                title = { Text("HK Radio") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xff121212)
                )
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
                        containerColor = Color(0xFF212121)
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF121212)
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
                                Color(0xFF212121)
                            else
                                Color.Transparent
                        )
                    )
                    Divider()
                }
            }
        }
    }
}