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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VpnRepository(database.serverDao())

        setContent {
            val vpnViewModel: VpnViewModel = viewModel(
                factory = VpnViewModel.Factory(repository)
            )
            val isDarkTheme by vpnViewModel.isDarkTheme.collectAsStateWithLifecycle()

            AetherVpnTheme(isDarkTheme = isDarkTheme) {
                AstralRootApp(vpnViewModel = vpnViewModel)
            }
        }
    }
}

private data class FloatingTabItem(
    val icon: ImageVector,
    val contentDescription: String,
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

    val appStage by vpnViewModel.appStage.collectAsStateWithLifecycle()
    val isDarkTheme by vpnViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isEnglish by vpnViewModel.isEnglish.collectAsStateWithLifecycle()
    val operatorName by vpnViewModel.operatorName.collectAsStateWithLifecycle()
    val operatorUid by vpnViewModel.operatorUid.collectAsStateWithLifecycle()
    val avatarFilePath by vpnViewModel.avatarFilePath.collectAsStateWithLifecycle()

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
    val snackbarMessage by vpnViewModel.snackbarMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<VlessServerEntity?>(null) }
    var inspectingXrayServer by remember { mutableStateOf<VlessServerEntity?>(null) }

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            vpnViewModel.clearSnackbar()
        }
    }

    BackHandler(
        enabled = isManualOfflinePreview ||
            appStage == AppStage.ADVANTAGES ||
            (appStage == AppStage.MAIN_ENCLAVE && currentTab != 0)
    ) {
        if (isManualOfflinePreview) {
            isManualOfflinePreview = false
        } else if (appStage == AppStage.ADVANTAGES) {
            vpnViewModel.backToWelcome()
        } else if (currentTab != 0) {
            currentTab = 0
        }
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

    val floatingTabs = remember {
        listOf(
            FloatingTabItem(Icons.Default.Key, "Tunnel Key", "nav_tab_tunnel"),
            FloatingTabItem(Icons.Default.Dns, "Global Nodes", "nav_tab_servers"),
            FloatingTabItem(Icons.Default.Person, "Sovereign Operator", "nav_tab_profile")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.primaryInk,
                    contentColor = colors.onPrimaryInk
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = appStage,
                animationSpec = tween(320),
                label = "stage_crossfade"
            ) { stage ->
                when (stage) {
                    AppStage.WELCOME -> {
                        WelcomeScreen(
                            isEnglish = isEnglish,
                            isDarkTheme = isDarkTheme,
                            onToggleLanguage = vpnViewModel::toggleLanguage,
                            onToggleTheme = vpnViewModel::toggleTheme,
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
                            when (currentTab) {
                                0 -> DashboardScreen(
                                    connectionStatus = connectionStatus,
                                    connectedSeconds = connectedSeconds,
                                    selectedServer = selectedServer,
                                    allServers = allServers,
                                    isEnglish = isEnglish,
                                    isDarkTheme = isDarkTheme,
                                    onToggleLanguage = vpnViewModel::toggleLanguage,
                                    onToggleTheme = vpnViewModel::toggleTheme,
                                    onToggleConnection = handleToggleConnection,
                                    onSelectServer = { srv -> vpnViewModel.selectServer(srv, context) },
                                    onOpenServerList = { currentTab = 1 },
                                    onOpenAddServer = {
                                        editingServer = null
                                        showAddOrEditDialog = true
                                    },
                                    onOpenXrayConfig = { srv -> inspectingXrayServer = srv },
                                    onDeleteServer = vpnViewModel::deleteServer,
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
                                    onBackClick = { currentTab = 0 },
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

                            // Floating 3-Icon Capsule Bottom Bar
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 14.dp)
                                    .width(228.dp)
                                    .height(62.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(50),
                                        ambientColor = Color.Black.copy(alpha = 0.25f)
                                    )
                                    .border(1.dp, colors.border, RoundedCornerShape(50)),
                                shape = RoundedCornerShape(50),
                                color = colors.card.copy(alpha = 0.96f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    floatingTabs.forEachIndexed { idx, item ->
                                        val isSelected = currentTab == idx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    if (isSelected) colors.primaryInk else Color.Transparent
                                                )
                                                .clickable { currentTab = idx }
                                                .testTag(item.tag),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.contentDescription,
                                                tint = if (isSelected) colors.onPrimaryInk else colors.secondaryText,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Animated "No internet connection" Overlay (triggers automatically when offline or via header toggle)
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
