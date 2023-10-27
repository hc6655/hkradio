package com.example.hkradio

import android.app.*
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.hkradio.ui.theme.HKRadioTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors


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
                RadioPlayer.setController(controller)
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onDestroy() {
        RadioPlayer.stop()
        MediaController.releaseFuture(controllerFuture)
        super.onDestroy()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldScreen() {
    val isPlaying by RadioPlayer.isPlaying.observeAsState(false)
    val isPlayerLoading by RadioPlayer.isPlayerLoading.observeAsState(false)
    val isPause by RadioPlayer.isPause.observeAsState(false)
    var isLoading by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ChannelData?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = selected) {
        if (selected != null) {
            isLoading = true
            RadioPlayer.play(selected!!)
        }
    }

    LaunchedEffect(key1 = isPlaying) {
        if (isPlaying)
            isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("香港收音機") },
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

                        val image = if (isPlaying)
                            Icons.Filled.Pause
                        else
                            Icons.Filled.PlayArrow

                        IconButton(
                            onClick = {
                                if (!isLoading) {
                                    if (!isPlaying)
                                        isLoading = true
                                    RadioPlayer.toggle()
                                }
                            },
                            modifier = Modifier.size(60.dp)
                        ) {
                            if (!isPlaying && isPlayerLoading && !isPause) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(30.dp),
                                    color = Color.White
                                )
                            } else {
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
                                Color(0xFF414141)
                            else
                                Color.Transparent
                        ),
                        leadingContent = {
                            val color = if (selected == channel) Color(0xDF000000) else Color.Transparent
                            Image(
                                painter = painterResource(id = channel.artwork),
                                contentDescription = "Image",
                                modifier = Modifier.size(50.dp),
                                colorFilter = ColorFilter.tint(color, blendMode = BlendMode.Darken)
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }
}