package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.AccountRepository
import com.example.data.AppDatabase
import com.example.data.VlessServerEntity
import com.example.data.VpnRepository
import com.example.ui.AppStage
import com.example.ui.VpnConnectionStatus
import com.example.ui.VpnViewModel
import com.example.ui.components.AddOrEditVlessDialog
import com.example.ui.components.XrayConfigInspectorDialog
import com.example.ui.screens.AdvantagesScreen
import com.example.ui.screens.ConnectingWaveScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NoInternetConnectionOverlay
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AetherVpnTheme
import com.example.ui.theme.LocalAstralColors
import com.example.ui.theme.PlusJakartaFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VpnRepository(database.serverDao())
        val accountRepository = runCatching { AccountRepository(applicationContext) }.getOrNull()
        val prefs = applicationContext.getSharedPreferences("bush_vpn_prefs", Context.MODE_PRIVATE)

        setContent {
            val vpnViewModel: VpnViewModel = viewModel(
                factory = VpnViewModel.Factory(
                    repository = repository,
                    prefs = prefs,
                    accountDao = database.accountDao(),
                    accountRepository = accountRepository
                )
            )
            val isDarkTheme by vpnViewModel.isDarkTheme.collectAsStateWithLifecycle()

            AetherVpnTheme(isDarkTheme = isDarkTheme) {
                AstralRootApp(vpnViewModel = vpnViewModel)
            }
        }
    }
}

private data class BottomTabItem(
    val icon: ImageVector,
    val labelRu: String,
    val labelEn: String,
    val tag: String
)

private fun isNetworkCurrentlyOnline(context: Context): Boolean {
    return runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(true)
}

@Composable
fun AstralRootApp(vpnViewModel: VpnViewModel) {
    val context = LocalContext.current
    val colors = LocalAstralColors.current
    val pagerScope = rememberCoroutineScope()

    val appStage by vpnViewModel.appStage.collectAsStateWithLifecycle()
    val isDarkTheme by vpnViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isEnglish by vpnViewModel.isEnglish.collectAsStateWithLifecycle()
    val isRegistrationCompleted by vpnViewModel.isRegistrationCompleted.collectAsStateWithLifecycle()
    val operatorName by vpnViewModel.operatorName.collectAsStateWithLifecycle()
    val operatorEmail by vpnViewModel.operatorEmail.collectAsStateWithLifecycle()
    val operatorUsername by vpnViewModel.operatorUsername.collectAsStateWithLifecycle()
    val operatorUid by vpnViewModel.operatorUid.collectAsStateWithLifecycle()
    val avatarFilePath by vpnViewModel.avatarFilePath.collectAsStateWithLifecycle()
    val latestVerificationCode by vpnViewModel.latestVerificationCode.collectAsStateWithLifecycle()
    val emailDispatchResult by vpnViewModel.emailDispatchResult.collectAsStateWithLifecycle()
    val smtpConfig by vpnViewModel.smtpConfig.collectAsStateWithLifecycle()

    val connectionStatus by vpnViewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectedSeconds by vpnViewModel.connectedSeconds.collectAsStateWithLifecycle()
    val selectedServer by vpnViewModel.selectedServer.collectAsStateWithLifecycle()
    val allServers by vpnViewModel.allServers.collectAsStateWithLifecycle()
    val filteredServers by vpnViewModel.filteredServers.collectAsStateWithLifecycle()
    val searchQuery by vpnViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by vpnViewModel.selectedFilter.collectAsStateWithLifecycle()
    val isPingingAll by vpnViewModel.isPingingAll.collectAsStateWithLifecycle()
    val pingingServerIds by vpnViewModel.pingingServerIds.collectAsStateWithLifecycle()
    val settings by vpnViewModel.settings.collectAsStateWithLifecycle()
    val coreLogs by vpnViewModel.coreLogs.collectAsStateWithLifecycle()
    val snackbarMessage by vpnViewModel.snackbarMessage.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<VlessServerEntity?>(null) }
    var inspectingXrayServer by remember { mutableStateOf<VlessServerEntity?>(null) }

    // Top T-Bank / iOS Notification Island state
    var islandMessage by remember { mutableStateOf<String?>(null) }
    var showConnectedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            islandMessage = msg
            vpnViewModel.clearSnackbar()
            delay(2600)
            if (islandMessage == msg) {
                islandMessage = null
            }
        }
    }

    LaunchedEffect(connectionStatus) {
        if (connectionStatus == VpnConnectionStatus.CONNECTED) {
            showConnectedBanner = true
            delay(2400)
            showConnectedBanner = false
        } else {
            showConnectedBanner = false
        }
    }

    // Real system network state + manual offline preview state
    var isSystemOffline by remember { mutableStateOf(!isNetworkCurrentlyOnline(context)) }
    var isManualOfflinePreview by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isSystemOffline = false
            }

            override fun onLost(network: Network) {
                isSystemOffline = !isNetworkCurrentlyOnline(context)
            }
        }
        runCatching { cm?.registerDefaultNetworkCallback(callback) }
        onDispose {
            runCatching { cm?.unregisterNetworkCallback(callback) }
        }
    }

    BackHandler(
        enabled = isManualOfflinePreview ||
            appStage == AppStage.ADVANTAGES ||
            (appStage == AppStage.MAIN_ENCLAVE && pagerState.currentPage != 0)
    ) {
        if (isManualOfflinePreview) {
            isManualOfflinePreview = false
        } else if (appStage == AppStage.ADVANTAGES) {
            vpnViewModel.backToWelcome()
        } else if (pagerState.currentPage != 0) {
            pagerScope.launch {
                pagerState.animateScrollToPage(0, animationSpec = tween(300, easing = FastOutSlowInEasing))
            }
        }
    }

    val handleSendEmailCode: (String) -> Unit = { email ->
        vpnViewModel.sendEmailVerificationCode(context, email)
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        vpnViewModel.toggleConnection(context)
    }

    val handleToggleConnection: () -> Unit = {
        if (connectionStatus == VpnConnectionStatus.DISCONNECTED) {
            val prepareIntent = runCatching { VpnService.prepare(context) }.getOrNull()
            if (prepareIntent != null) {
                runCatching {
                    vpnPermissionLauncher.launch(prepareIntent)
                }.onFailure {
                    vpnViewModel.toggleConnection(context)
                }
            } else {
                vpnViewModel.toggleConnection(context)
            }
        } else {
            vpnViewModel.toggleConnection(context)
        }
    }

    val bottomTabs = remember {
        listOf(
            BottomTabItem(Icons.Default.Home, "Главная", "Home", "nav_tab_tunnel"),
            BottomTabItem(Icons.Default.Dns, "Серверы", "Servers", "nav_tab_servers"),
            BottomTabItem(Icons.Default.Person, "Профиль", "Profile", "nav_tab_profile")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = appStage,
                transitionSpec = {
                    val forward = targetState.ordinal >= initialState.ordinal
                    val enterSlide = slideInHorizontally(
                        animationSpec = tween(340, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> if (forward) fullWidth / 3 else -fullWidth / 3 }
                    ) + fadeIn(animationSpec = tween(280))
                    val exitSlide = slideOutHorizontally(
                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 }
                    ) + fadeOut(animationSpec = tween(240))
                    enterSlide togetherWith exitSlide
                },
                label = "stage_slide_transition"
            ) { stage ->
                when (stage) {
                    AppStage.WELCOME -> {
                        WelcomeScreen(
                            isEnglish = isEnglish,
                            isDarkTheme = isDarkTheme,
                            isRegistrationCompleted = isRegistrationCompleted,
                            initialEmail = operatorEmail,
                            initialName = operatorName,
                            initialUsername = operatorUsername,
                            latestVerificationCode = latestVerificationCode,
                            emailDispatchResult = emailDispatchResult,
                            smtpConfig = smtpConfig,
                            onSaveSmtpConfig = vpnViewModel::saveSmtpConfig,
                            onSendEmailCode = handleSendEmailCode,
                            onVerifyEmailCode = vpnViewModel::verifyEmailCode,
                            onToggleLanguage = vpnViewModel::toggleLanguage,
                            onToggleTheme = vpnViewModel::toggleTheme,
                            onCompleteRegistration = vpnViewModel::completeRegistration,
                            onStartClick = vpnViewModel::goToAdvantages
                        )
                    }

                    AppStage.ADVANTAGES -> {
                        AdvantagesScreen(
                            isEnglish = isEnglish,
                            onBack = vpnViewModel::backToWelcome,
                            onContinue = vpnViewModel::startConnectingTransitionToMain
                        )
                    }

                    AppStage.CONNECTING_TRANSITION -> {
                        ConnectingWaveScreen(isEnglish = isEnglish)
                    }

                    AppStage.MAIN_ENCLAVE -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Smooth Swipeable HorizontalPager across the 3 main screens
                            HorizontalPager(
                                state = pagerState,
                                beyondViewportPageCount = 1,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 86.dp)
                            ) { page ->
                                val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                ).absoluteValue.coerceIn(0f, 1f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val scale = 1f - (0.04f * pageOffset)
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = 1f - (0.20f * pageOffset)
                                        }
                                ) {
                                    when (page) {
                                        0 -> DashboardScreen(
                                            connectionStatus = connectionStatus,
                                            connectedSeconds = connectedSeconds,
                                            selectedServer = selectedServer,
                                            allServers = allServers,
                                            isEnglish = isEnglish,
                                            isDarkTheme = isDarkTheme,
                                            isPingingAll = isPingingAll,
                                            pingingServerIds = pingingServerIds,
                                            settings = settings,
                                            coreLogs = coreLogs,
                                            onUpdateSettings = vpnViewModel::updateSettings,
                                            onClearLogs = vpnViewModel::clearLogs,
                                            onToggleLanguage = vpnViewModel::toggleLanguage,
                                            onToggleTheme = vpnViewModel::toggleTheme,
                                            onToggleConnection = handleToggleConnection,
                                            onSelectServer = { srv -> vpnViewModel.selectServer(srv, context) },
                                            onToggleFavorite = vpnViewModel::toggleFavorite,
                                            onOpenServerList = {
                                                pagerScope.launch {
                                                    pagerState.animateScrollToPage(
                                                        1,
                                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                                    )
                                                }
                                            },
                                            onOpenAddServer = {
                                                editingServer = null
                                                showAddOrEditDialog = true
                                            },
                                            onOpenXrayConfig = { srv -> inspectingXrayServer = srv },
                                            onPingServer = vpnViewModel::pingSingleServer,
                                            onPingAll = vpnViewModel::pingAllServers,
                                            onImportSubscriptionText = vpnViewModel::importSubscriptionOrUri,
                                            onCopiedMessage = vpnViewModel::showMessage,
                                            onDeleteServer = vpnViewModel::deleteServer,
                                            onReplayOnboarding = vpnViewModel::replayOnboarding,
                                            onTriggerNoInternetDemo = { isManualOfflinePreview = true }
                                        )

                                        1 -> ServersScreen(
                                            servers = filteredServers,
                                            allServers = allServers,
                                            selectedServerId = selectedServer?.id,
                                            searchQuery = searchQuery,
                                            selectedFilter = selectedFilter,
                                            isEnglish = isEnglish,
                                            isPingingAll = isPingingAll,
                                            pingingServerIds = pingingServerIds,
                                            onBackClick = {
                                                pagerScope.launch {
                                                    pagerState.animateScrollToPage(
                                                        0,
                                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                                    )
                                                }
                                            },
                                            onSearchChange = vpnViewModel::setSearchQuery,
                                            onFilterChange = vpnViewModel::setFilter,
                                            onSelectServer = { srv ->
                                                vpnViewModel.selectServer(srv, context)
                                            },
                                            onToggleFavorite = vpnViewModel::toggleFavorite,
                                            onPingServer = vpnViewModel::pingSingleServer,
                                            onPingAll = vpnViewModel::pingAllServers,
                                            onAddServerClick = {
                                                editingServer = null
                                                showAddOrEditDialog = true
                                            },
                                            onDeleteServerClick = vpnViewModel::deleteServer,
                                            onOpenXrayConfig = { srv -> inspectingXrayServer = srv },
                                            onImportSubscriptionText = vpnViewModel::importSubscriptionOrUri,
                                            onToggleEuropePack = vpnViewModel::toggleEuropeSubscriptionPack,
                                            onCopiedMessage = vpnViewModel::showMessage
                                        )

                                        2 -> ProfileScreen(
                                            operatorName = operatorName,
                                            operatorUsername = operatorUsername,
                                            operatorEmail = operatorEmail,
                                            operatorUid = operatorUid,
                                            avatarFilePath = avatarFilePath,
                                            isEnglish = isEnglish,
                                            isDarkTheme = isDarkTheme,
                                            onUpdateOperatorName = vpnViewModel::updateOperatorName,
                                            onPickAvatarUri = { uri -> vpnViewModel.saveOperatorAvatar(context, uri) },
                                            onToggleTheme = vpnViewModel::toggleTheme,
                                            onToggleLanguage = vpnViewModel::toggleLanguage,
                                            onReplayOnboarding = vpnViewModel::replayOnboarding,
                                            onPreviewNoInternet = { isManualOfflinePreview = true },
                                            onCopiedMessage = vpnViewModel::showMessage
                                        )
                                    }
                                }
                            }

                            // Full-width Apple iOS / SwiftUI Blur TabBar
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .border(width = 0.5.dp, color = Color(0x1FFFFFFF), shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)),
                                color = Color(0xEB121214)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .height(58.dp)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    bottomTabs.forEachIndexed { idx, item ->
                                        val isSelected = pagerState.targetPage == idx
                                        val itemTint by animateColorAsState(
                                            targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF8E8E93),
                                            animationSpec = tween(180),
                                            label = "apple_tab_tint_$idx"
                                        )

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    pagerScope.launch {
                                                        pagerState.animateScrollToPage(
                                                            idx,
                                                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                                                        )
                                                    }
                                                }
                                                .padding(vertical = 4.dp)
                                                .testTag(item.tag),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = if (isEnglish) item.labelEn else item.labelRu,
                                                tint = itemTint,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = if (isEnglish) item.labelEn else item.labelRu,
                                                fontFamily = PlusJakartaFontFamily,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 10.5.sp,
                                                letterSpacing = (-0.2).sp,
                                                color = itemTint,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // T-Bank / iOS Top Floating Notification Island ("Подключаемся", "Подключено", or notifications)
            val isIslandVisible = connectionStatus == VpnConnectionStatus.CONNECTING ||
                showConnectedBanner ||
                !islandMessage.isNullOrBlank()

            AnimatedVisibility(
                visible = isIslandVisible,
                enter = slideInVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    initialOffsetY = { -it * 2 }
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                    targetOffsetY = { -it * 2 }
                ) + fadeOut(tween(180)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                val activeNodeTitle = selectedServer?.displayTitle(isEnglish)
                    ?: allServers.firstOrNull()?.displayTitle(isEnglish)
                    ?: "VLESS"
                val displayLabel = when {
                    !islandMessage.isNullOrBlank() -> islandMessage.orEmpty()
                    connectionStatus == VpnConnectionStatus.CONNECTING ->
                        if (isEnglish) "Connecting..." else "Подключаемся"
                    showConnectedBanner ->
                        if (isEnglish) "Connected • $activeNodeTitle" else "Подключено • $activeNodeTitle"
                    else -> ""
                }

                Surface(
                    modifier = Modifier
                        .widthIn(min = 160.dp, max = 330.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color.Black.copy(alpha = 0.35f)
                        )
                        .border(1.dp, colors.border, RoundedCornerShape(50))
                        .clickable {
                            islandMessage = null
                            showConnectedBanner = false
                        },
                    shape = RoundedCornerShape(50),
                    color = colors.cardElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (connectionStatus == VpnConnectionStatus.CONNECTING && islandMessage.isNullOrBlank()) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.8.dp,
                                color = colors.primaryInk
                            )
                        } else if (showConnectedBanner && islandMessage.isNullOrBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primaryInk,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryInk.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = colors.primaryInk,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = displayLabel,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.primaryInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Animated "No internet connection" Overlay
            AnimatedVisibility(
                visible = isSystemOffline || isManualOfflinePreview,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                NoInternetConnectionOverlay(
                    isEnglish = isEnglish,
                    onRetry = {
                        isSystemOffline = !isNetworkCurrentlyOnline(context)
                    },
                    onDismiss = {
                        isManualOfflinePreview = false
                        isSystemOffline = !isNetworkCurrentlyOnline(context)
                    }
                )
            }
        }
    }

    if (showAddOrEditDialog) {
        AddOrEditVlessDialog(
            initialServer = editingServer,
            onDismiss = {
                showAddOrEditDialog = false
                editingServer = null
            },
            onImportUri = { uri ->
                vpnViewModel.importVlessUri(uri) {
                    showAddOrEditDialog = false
                }
            },
            onSaveCustomServer = { server, isEdit ->
                vpnViewModel.saveCustomServer(server, isEdit)
            }
        )
    }

    val xrayTarget = inspectingXrayServer
    if (xrayTarget != null) {
        XrayConfigInspectorDialog(
            server = xrayTarget,
            settings = settings,
            onDismiss = { inspectingXrayServer = null },
            onCopied = { msg -> vpnViewModel.showMessage(msg) }
        )
    }
}
