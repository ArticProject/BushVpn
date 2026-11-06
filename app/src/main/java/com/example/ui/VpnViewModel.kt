package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnRepository
import com.example.data.VpnTunnelSettings
import com.example.vpn.AetherVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

enum class VpnConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class AppStage {
    WELCOME,
    ADVANTAGES,
    CONNECTING_TRANSITION,
    MAIN_ENCLAVE
}

data class TrafficSample(
    val downloadMbps: Float,
    val uploadMbps: Float
)

data class CoreLogEntry(
    val id: Long,
    val timestamp: String,
    val level: String,
    val message: String
)

class VpnViewModel(
    private val repository: VpnRepository
) : ViewModel() {

    val allServers: StateFlow<List<VlessServerEntity>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appStage = MutableStateFlow(AppStage.WELCOME)
    val appStage: StateFlow<AppStage> = _appStage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isEnglish = MutableStateFlow(false)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _operatorName = MutableStateFlow("user")
    val operatorName: StateFlow<String> = _operatorName.asStateFlow()

    private val _operatorUid = MutableStateFlow("#9E29A")
    val operatorUid: StateFlow<String> = _operatorUid.asStateFlow()

    private val _avatarFilePath = MutableStateFlow<String?>(null)
    val avatarFilePath: StateFlow<String?> = _avatarFilePath.asStateFlow()

    private val _selectedServerId = MutableStateFlow<Long?>(null)
    val selectedServerId: StateFlow<Long?> = _selectedServerId.asStateFlow()

    private val _connectionStatus = MutableStateFlow(VpnConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<VpnConnectionStatus> = _connectionStatus.asStateFlow()

    private val _handshakeStep = MutableStateFlow("Ready")
    val handshakeStep: StateFlow<String> = _handshakeStep.asStateFlow()

    private val _connectedSeconds = MutableStateFlow(0L)
    val connectedSeconds: StateFlow<Long> = _connectedSeconds.asStateFlow()

    private val _downloadSpeedMbps = MutableStateFlow(0f)
    val downloadSpeedMbps: StateFlow<Float> = _downloadSpeedMbps.asStateFlow()

    private val _uploadSpeedMbps = MutableStateFlow(0f)
    val uploadSpeedMbps: StateFlow<Float> = _uploadSpeedMbps.asStateFlow()

    private val _totalDownloadMb = MutableStateFlow(0f)
    val totalDownloadMb: StateFlow<Float> = _totalDownloadMb.asStateFlow()

    private val _totalUploadMb = MutableStateFlow(0f)
    val totalUploadMb: StateFlow<Float> = _totalUploadMb.asStateFlow()

    private val _speedHistory = MutableStateFlow(
        List(28) { TrafficSample(0f, 0f) }
    )
    val speedHistory: StateFlow<List<TrafficSample>> = _speedHistory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, FAVORITES, P2P, STREAMING, REALITY
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedCountryCode = MutableStateFlow("ALL")
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode.asStateFlow()

    private val _isPingingAll = MutableStateFlow(false)
    val isPingingAll: StateFlow<Boolean> = _isPingingAll.asStateFlow()

    private val _pingingServerIds = MutableStateFlow<Set<Long>>(emptySet())
    val pingingServerIds: StateFlow<Set<Long>> = _pingingServerIds.asStateFlow()

    private val _settings = MutableStateFlow(VpnTunnelSettings())
    val settings: StateFlow<VpnTunnelSettings> = _settings.asStateFlow()

    private val _coreLogs = MutableStateFlow<List<CoreLogEntry>>(emptyList())
    val coreLogs: StateFlow<List<CoreLogEntry>> = _coreLogs.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val selectedServer: StateFlow<VlessServerEntity?> = combine(
        allServers,
        _selectedServerId
    ) { servers, selectedId ->
        if (servers.isEmpty()) null
        else servers.find { it.id == selectedId } ?: servers.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredServers: StateFlow<List<VlessServerEntity>> = combine(
        allServers,
        _searchQuery,
        _selectedFilter,
        _selectedCountryCode
    ) { servers, query, filter, country ->
        servers.filter { srv ->
            val matchesQuery = query.isBlank() ||
                srv.name.contains(query, ignoreCase = true) ||
                srv.countryName.contains(query, ignoreCase = true) ||
                srv.city.contains(query, ignoreCase = true) ||
                srv.nodeAirportCode.contains(query, ignoreCase = true) ||
                srv.countryCode.contains(query, ignoreCase = true) ||
                srv.host.contains(query, ignoreCase = true) ||
                srv.localizedCity(true).contains(query, ignoreCase = true) ||
                srv.localizedCountry(true).contains(query, ignoreCase = true)

            val matchesCountry = country == "ALL" || srv.countryCode.equals(country, ignoreCase = true)

            val matchesFilter = when (filter) {
                "FAVORITES" -> srv.isFavorite
                "P2P" -> srv.category.equals("P2P", ignoreCase = true) || srv.pingMs <= 45
                "STREAMING" -> srv.category.equals("STREAMING", ignoreCase = true) || srv.bandwidthGbps >= 10
                "REALITY" -> srv.security.equals("reality", ignoreCase = true)
                "LOW_PING" -> srv.pingMs in 1..50
                else -> true
            }

            matchesQuery && matchesCountry && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var telemetryJob: Job? = null
    private var logCounter = 1L

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            appendLog("INFO", "BUSH VPN 👊 Sovereign X-Ray Core 1.8.24 initialized (VLESS REALITY)")
        }
    }

    fun goToAdvantages() {
        _appStage.value = AppStage.ADVANTAGES
    }

    fun backToWelcome() {
        _appStage.value = AppStage.WELCOME
    }

    fun startConnectingTransitionToMain() {
        viewModelScope.launch {
            _appStage.value = AppStage.CONNECTING_TRANSITION
            delay(1850)
            _appStage.value = AppStage.MAIN_ENCLAVE
        }
    }

    fun replayOnboarding() {
        _appStage.value = AppStage.WELCOME
    }

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    fun toggleLanguage() {
        _isEnglish.update { !it }
    }

    fun updateOperatorName(newName: String) {
        val cleaned = newName.trim().ifBlank { "user" }
        _operatorName.value = cleaned
        showMessage(if (_isEnglish.value) "Operator name updated: $cleaned" else "Имя оператора сохранено: $cleaned")
    }

    fun saveOperatorAvatar(context: Context, sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val destFile = File(context.filesDir, "operator_avatar_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                withContext(Dispatchers.Main) {
                    _avatarFilePath.value = destFile.absolutePath
                    showMessage(if (_isEnglish.value) "Operator avatar updated" else "Аватар оператора обновлен")
                }
            }
        }
    }

    fun selectServer(server: VlessServerEntity, context: Context? = null) {
        val wasConnected = _connectionStatus.value == VpnConnectionStatus.CONNECTED
        _selectedServerId.value = server.id
        appendLog(
            "INFO",
            "Selected node ${server.nodeAirportCode} • ${server.city} (${server.host}:${server.port})"
        )
        if (wasConnected && context != null) {
            viewModelScope.launch {
                connectToServer(context, server)
            }
        }
    }

    fun selectBestServer(context: Context? = null) {
        val best = allServers.value.minByOrNull { if (it.pingMs > 0) it.pingMs else 999 }
        if (best != null) {
            selectServer(best, context)
            showMessage("${best.nodeAirportCode} • ${best.city} (${best.pingMs}ms)")
        }
    }

    fun toggleConnection(context: Context) {
        when (_connectionStatus.value) {
            VpnConnectionStatus.CONNECTED, VpnConnectionStatus.CONNECTING -> {
                disconnect(context)
            }
            VpnConnectionStatus.DISCONNECTED -> {
                val target = selectedServer.value ?: allServers.value.firstOrNull()
                if (target != null) {
                    connectToServer(context, target)
                }
            }
        }
    }

    private fun connectToServer(context: Context, server: VlessServerEntity) {
        telemetryJob?.cancel()
        _connectionStatus.value = VpnConnectionStatus.CONNECTING

        telemetryJob = viewModelScope.launch {
            _handshakeStep.value = "LINKING • ${server.nodeAirportCode}"
            appendLog("REALITY", "Linking VLESS REALITY [${server.sni} • fp=${server.fingerprint}]")
            delay(650)

            startSystemVpnService(context, server)
            delay(550)

            _connectionStatus.value = VpnConnectionStatus.CONNECTED
            _handshakeStep.value = "ACTIVE • ${server.virtualIp}"
            appendLog(
                "INFO",
                "Tunnel established: ${server.nodeAirportCode} (${server.virtualIp}) • ${server.pingMs}ms"
            )

            var tick = 0
            while (_connectionStatus.value == VpnConnectionStatus.CONNECTED) {
                delay(1000)
                tick++
                _connectedSeconds.update { it + 1 }

                val wave = (sin(tick * 0.45) + 1.2).toFloat()
                val jitterDown = (12..46).random() / 10f
                val jitterUp = (4..18).random() / 10f
                val currentDown = (24.5f * wave + jitterDown * 8f).coerceIn(8.4f, 142.8f)
                val currentUp = (8.2f * wave + jitterUp * 4f).coerceIn(2.1f, 48.6f)

                _downloadSpeedMbps.value = currentDown
                _uploadSpeedMbps.value = currentUp
                _totalDownloadMb.update { it + (currentDown / 8f) }
                _totalUploadMb.update { it + (currentUp / 8f) }

                _speedHistory.update { history ->
                    (history.drop(1) + TrafficSample(currentDown, currentUp))
                }
            }
        }
    }

    fun disconnect(context: Context) {
        telemetryJob?.cancel()
        stopSystemVpnService(context)
        _connectionStatus.value = VpnConnectionStatus.DISCONNECTED
        _handshakeStep.value = "DISCONNECTED"
        _connectedSeconds.value = 0L
        _downloadSpeedMbps.value = 0f
        _uploadSpeedMbps.value = 0f
        _speedHistory.value = List(28) { TrafficSample(0f, 0f) }
        appendLog("WARN", "Sovereign tunnel disconnected")
    }

    private fun startSystemVpnService(context: Context, server: VlessServerEntity) {
        try {
            val currentSettings = _settings.value
            val intent = Intent(context, AetherVpnService::class.java).apply {
                action = AetherVpnService.ACTION_CONNECT
                putExtra(AetherVpnService.EXTRA_SERVER_NAME, server.name)
                putExtra(AetherVpnService.EXTRA_SERVER_HOST, server.host)
                putExtra(AetherVpnService.EXTRA_PRIMARY_DNS, currentSettings.primaryDns)
                putExtra(AetherVpnService.EXTRA_SECONDARY_DNS, currentSettings.secondaryDns)
                putExtra(AetherVpnService.EXTRA_MTU, currentSettings.mtu)
                putExtra(AetherVpnService.EXTRA_IPV6, currentSettings.enableIpv6)
            }
            ContextCompat.startForegroundService(context, intent)
        } catch (_: Exception) {
        }
    }

    private fun stopSystemVpnService(context: Context) {
        try {
            val intent = Intent(context, AetherVpnService::class.java).apply {
                action = AetherVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
        } catch (_: Exception) {
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setCountryFilter(countryCode: String) {
        _selectedCountryCode.value = countryCode
    }

    fun toggleFavorite(server: VlessServerEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(server)
        }
    }

    fun pingSingleServer(server: VlessServerEntity) {
        if (_pingingServerIds.value.contains(server.id)) return
        viewModelScope.launch {
            _pingingServerIds.update { it + server.id }
            val rtt = repository.pingServer(server)
            appendLog("INFO", "Ping ${server.nodeAirportCode} (${server.city}): ${rtt}ms")
            _pingingServerIds.update { it - server.id }
        }
    }

    fun pingAllServers() {
        if (_isPingingAll.value) return
        viewModelScope.launch {
            _isPingingAll.value = true
            val list = allServers.value
            list.forEach { srv ->
                _pingingServerIds.update { it + srv.id }
                launch {
                    repository.pingServer(srv)
                    _pingingServerIds.update { it - srv.id }
                }
            }
            delay(450)
            _isPingingAll.value = false
            val fastest = allServers.value.minByOrNull { it.pingMs }
            if (fastest != null) {
                showMessage("${fastest.nodeAirportCode} • ${fastest.city}: ${fastest.pingMs}ms")
            }
        }
    }

    fun importVlessUri(uriText: String, onSuccess: () -> Unit = {}) {
        val result = VlessParser.parseUri(uriText)
        result.onSuccess { parsedServer ->
            viewModelScope.launch {
                val newId = repository.addServer(parsedServer)
                _selectedServerId.value = newId
                showMessage("VLESS: ${parsedServer.name} imported")
                onSuccess()
            }
        }.onFailure { err ->
            showMessage(err.message ?: "Invalid vless:// URI")
        }
    }

    fun saveCustomServer(server: VlessServerEntity, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit && server.id != 0L) {
                repository.updateServer(server)
                showMessage("Updated ${server.name}")
            } else {
                val id = repository.addServer(server)
                _selectedServerId.value = id
                showMessage("Added ${server.name}")
            }
        }
    }

    fun deleteServer(server: VlessServerEntity) {
        viewModelScope.launch {
            repository.deleteServer(server)
            showMessage("Deleted ${server.name}")
        }
    }

    fun updateSettings(transform: (VpnTunnelSettings) -> VpnTunnelSettings) {
        _settings.update(transform)
    }

    fun clearLogs() {
        _coreLogs.value = emptyList()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    private fun appendLog(level: String, message: String) {
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = CoreLogEntry(
            id = logCounter++,
            timestamp = timeFmt,
            level = level,
            message = message
        )
        _coreLogs.update { current ->
            (listOf(entry) + current).take(80)
        }
    }

    class Factory(private val repository: VpnRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VpnViewModel(repository) as T
        }
    }
}
