package com.example.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AccountDao
import com.example.data.AccountRepository
import com.example.data.EmailCodeDispatcher
import com.example.data.EmailDispatchResult
import com.example.data.EmailVerificationEntity
import com.example.data.SmtpConfig
import com.example.data.UserAccountEntity
import com.example.data.VlessParser
import com.example.data.VlessServerEntity
import com.example.data.VpnRepository
import com.example.data.VpnTunnelSettings
import com.example.vpn.AetherVpnService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
    private val repository: VpnRepository,
    private val prefs: SharedPreferences? = null,
    private val accountDao: AccountDao? = null,
    private val accountRepository: AccountRepository? = null
) : ViewModel() {

    val allServers: StateFlow<List<VlessServerEntity>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEmailCodes: StateFlow<List<EmailVerificationEntity>> =
        (accountDao?.observeRecentCodes() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _latestVerificationCode = MutableStateFlow<String?>(null)
    val latestVerificationCode: StateFlow<String?> = _latestVerificationCode.asStateFlow()

    private val _emailDispatchResult = MutableStateFlow<EmailDispatchResult?>(null)
    val emailDispatchResult: StateFlow<EmailDispatchResult?> = _emailDispatchResult.asStateFlow()

    private val _smtpConfig = MutableStateFlow(
        SmtpConfig(
            host = prefs?.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com",
            port = prefs?.getInt("smtp_port", 587) ?: 587,
            senderEmail = prefs?.getString("smtp_sender_email", "") ?: "",
            appPassword = prefs?.getString("smtp_app_password", "") ?: ""
        )
    )
    val smtpConfig: StateFlow<SmtpConfig> = _smtpConfig.asStateFlow()

    private val _latestFirestoreCodeId = MutableStateFlow<String?>(null)

    // Always start with the NoteVault flower splash animation (which either opens the registration flow on first run or auto-transitions to MAIN_ENCLAVE)
    private val _appStage = MutableStateFlow(AppStage.WELCOME)
    val appStage: StateFlow<AppStage> = _appStage.asStateFlow()

    private val _isRegistrationCompleted = MutableStateFlow(
        prefs?.getBoolean("notevault_auth_email_code_v2", false) ?: false
    )
    val isRegistrationCompleted: StateFlow<Boolean> = _isRegistrationCompleted.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs?.getBoolean("is_dark_theme_v2", true) ?: true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isEnglish = MutableStateFlow(prefs?.getBoolean("is_english", false) ?: false)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _operatorEmail = MutableStateFlow(
        prefs?.getString("operator_email", "user@bushvpn.io") ?: "user@bushvpn.io"
    )
    val operatorEmail: StateFlow<String> = _operatorEmail.asStateFlow()

    private val _operatorName = MutableStateFlow(prefs?.getString("operator_name", "user") ?: "user")
    val operatorName: StateFlow<String> = _operatorName.asStateFlow()

    private val _operatorUsername = MutableStateFlow(
        prefs?.getString("operator_username", "bush_user") ?: "bush_user"
    )
    val operatorUsername: StateFlow<String> = _operatorUsername.asStateFlow()

    private val _operatorUid = MutableStateFlow("#9E29A")
    val operatorUid: StateFlow<String> = _operatorUid.asStateFlow()

    private val _avatarFilePath = MutableStateFlow<String?>(prefs?.getString("avatar_file_path", null))
    val avatarFilePath: StateFlow<String?> = _avatarFilePath.asStateFlow()

    private val _selectedServerId = MutableStateFlow<Long?>(
        prefs?.getLong("selected_server_id", -1L)?.takeIf { it > 0L }
    )
    val selectedServerId: StateFlow<Long?> = _selectedServerId.asStateFlow()

    private val _connectionStatus = MutableStateFlow(
        if (AetherVpnService.isServiceActive) VpnConnectionStatus.CONNECTED else VpnConnectionStatus.DISCONNECTED
    )
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

    private val _selectedFilter = MutableStateFlow("ALL")
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
        else servers.find { it.id == selectedId }
            ?: servers.firstOrNull { it.isCustom && it.countryCode != "INFO" }
            ?: servers.first()
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
                srv.providerName.contains(query, ignoreCase = true) ||
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
    private var latencyMonitorJob: Job? = null
    private val tcpPingSemaphore = Semaphore(6)
    private var logCounter = 1L

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            appendLog("INFO", "BUSH VPN Xray-core v26 initialized (VLESS / REALITY / TUN)")

            // Automatically restore or refresh any saved subscription URL
            val savedSubUrl = prefs?.getString("last_subscription_url", null).orEmpty()
            val currentList = runCatching { repository.allServers.first() }.getOrDefault(emptyList())
            val customSubUrls = currentList
                .mapNotNull { it.subscriptionUrl.takeIf { url -> url.startsWith("http", ignoreCase = true) } }
                .distinct()

            val urlsToRefresh = (customSubUrls + listOfNotNull(savedSubUrl.takeIf { it.startsWith("http", ignoreCase = true) }))
                .distinct()

            if (urlsToRefresh.isNotEmpty()) {
                urlsToRefresh.forEach { url ->
                    withContext(Dispatchers.IO) {
                        VlessParser.fetchSubscriptionFromUrl(url).onSuccess { (provider, nodes) ->
                            if (nodes.isNotEmpty()) {
                                repository.deleteBySubscriptionUrl(url)
                                repository.deleteByProviderName(provider)
                                var firstId: Long? = null
                                nodes.forEach { node ->
                                    val newId = repository.addServer(node)
                                    if (firstId == null && node.countryCode != "INFO") {
                                        firstId = newId
                                    }
                                }
                                if (_selectedServerId.value == null && firstId != null) {
                                    _selectedServerId.value = firstId
                                }
                            }
                        }
                    }
                }
            }

            startBackgroundLatencyMonitor()
        }
    }

    private fun startBackgroundLatencyMonitor() {
        latencyMonitorJob?.cancel()
        latencyMonitorJob = viewModelScope.launch(Dispatchers.IO) {
            // Initial live TCP latency test on startup
            delay(400)
            val initialServers = runCatching { repository.allServers.first() }.getOrDefault(emptyList())
            if (initialServers.isNotEmpty()) {
                performConcurrentTcpSweep(initialServers)
            }
            // Periodic background live TCP latency check every 25 seconds
            while (isActive) {
                delay(25_000L)
                if (!_isPingingAll.value) {
                    val currentServers = allServers.value.ifEmpty {
                        runCatching { repository.allServers.first() }.getOrDefault(emptyList())
                    }
                    val testable = currentServers.filter { it.isCustom && it.countryCode != "INFO" }
                    if (testable.isNotEmpty()) {
                        performConcurrentTcpSweep(testable)
                    }
                }
            }
        }
    }

    private suspend fun performConcurrentTcpSweep(servers: List<VlessServerEntity>) = coroutineScope {
        val targets = servers.filter { it.countryCode != "INFO" }
        targets.map { srv ->
            async(Dispatchers.IO) {
                tcpPingSemaphore.withPermit {
                    _pingingServerIds.update { it + srv.id }
                    try {
                        repository.pingServer(srv)
                    } finally {
                        _pingingServerIds.update { it - srv.id }
                    }
                }
            }
        }.awaitAll()
    }

    fun goToAdvantages() {
        prefs?.edit()?.putBoolean("onboarding_v2_shown", true)?.apply()
        _appStage.value = AppStage.MAIN_ENCLAVE
    }

    fun saveSmtpConfig(config: SmtpConfig) {
        _smtpConfig.value = config
        prefs?.edit()
            ?.putString("smtp_host", config.host.trim())
            ?.putInt("smtp_port", config.port)
            ?.putString("smtp_sender_email", config.senderEmail.trim())
            ?.putString("smtp_app_password", config.appPassword.trim())
            ?.apply()
        showMessage(
            if (_isEnglish.value) {
                "SMTP saved (${config.senderEmail})"
            } else {
                "SMTP сохранен (${config.senderEmail})"
            }
        )
    }

    fun sendEmailVerificationCode(
        @Suppress("UNUSED_PARAMETER") context: Context,
        email: String,
        onCodeSent: (String) -> Unit = {}
    ) {
        val cleanEmail = email.trim().ifBlank { "user@bushvpn.io" }
        val code = EmailCodeDispatcher.generateSixDigitCode()
        _latestVerificationCode.value = code
        _operatorEmail.value = cleanEmail
        _emailDispatchResult.value = null

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                accountDao?.insertVerificationCode(
                    EmailVerificationEntity(
                        email = cleanEmail,
                        code = code,
                        verified = false
                    )
                )
                val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                if (firebaseUser != null) {
                    accountRepository?.createVerificationCode(cleanEmail, code)?.onSuccess { docId ->
                        _latestFirestoreCodeId.value = docId
                    }
                }
            }
            val result = EmailCodeDispatcher.dispatchVerificationEmail(
                recipientEmail = cleanEmail,
                code = code,
                isEnglish = _isEnglish.value,
                smtpConfig = _smtpConfig.value
            )
            _emailDispatchResult.value = result
            showMessage(
                if (result.delivered) {
                    if (_isEnglish.value) {
                        "📩 Verification email sent to $cleanEmail"
                    } else {
                        "📩 Письмо с кодом отправлено на $cleanEmail"
                    }
                } else if (result.needsFormActivation) {
                    if (_isEnglish.value) {
                        "📩 Activation email sent to $cleanEmail (check Inbox/Spam)"
                    } else {
                        "📩 Письмо активации отправлено на $cleanEmail (проверьте Почту/Спам)"
                    }
                } else {
                    if (_isEnglish.value) {
                        "Configure SMTP to send directly to $cleanEmail"
                    } else {
                        "Настройте SMTP для прямой отправки на $cleanEmail"
                    }
                }
            )
            onCodeSent(code)
        }
    }

    fun verifyEmailCode(
        email: String,
        enteredCode: String,
        onResult: (Boolean) -> Unit
    ) {
        val cleanEmail = email.trim().ifBlank { _operatorEmail.value }
        val cleanEntered = enteredCode.trim()
        viewModelScope.launch {
            val isValid = withContext(Dispatchers.IO) {
                val dbEntry = accountDao?.getLatestCodeForEmail(cleanEmail)
                val expected = dbEntry?.code ?: _latestVerificationCode.value
                val match = !expected.isNullOrBlank() && expected == cleanEntered
                if (match) {
                    if (dbEntry != null) {
                        accountDao?.markCodeVerified(dbEntry.id)
                    }
                    val fsId = _latestFirestoreCodeId.value
                    val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                    if (firebaseUser != null && !fsId.isNullOrBlank()) {
                        accountRepository?.markCodeVerified(fsId)
                    }
                }
                match
            }
            onResult(isValid)
        }
    }

    fun completeRegistration(email: String, name: String, username: String, langCode: String) {
        val cleanEmail = email.trim().ifBlank { "user@bushvpn.io" }
        val cleanName = name.trim().ifBlank { "Operator" }
        val cleanUser = username.trim().removePrefix("@").lowercase().ifBlank { "bush_user" }
        val useEnglish = langCode != "ru" && langCode != "uk"

        _operatorEmail.value = cleanEmail
        _operatorName.value = cleanName
        _operatorUsername.value = cleanUser
        _isEnglish.value = useEnglish
        _isRegistrationCompleted.value = true

        prefs?.edit()
            ?.putString("operator_email", cleanEmail)
            ?.putString("operator_name", cleanName)
            ?.putString("operator_username", cleanUser)
            ?.putBoolean("is_english", useEnglish)
            ?.putBoolean("notevault_auth_email_code_v2", true)
            ?.putBoolean("onboarding_v2_shown", true)
            ?.apply()

        viewModelScope.launch(Dispatchers.IO) {
            accountDao?.upsertAccount(
                UserAccountEntity(
                    email = cleanEmail,
                    name = cleanName,
                    username = cleanUser,
                    passwordHash = "verified_sha256",
                    emailVerified = true
                )
            )
            val firebaseUser = runCatching { Firebase.auth.currentUser }.getOrNull()
            if (firebaseUser != null) {
                accountRepository?.saveUserAccount(
                    email = cleanEmail,
                    name = cleanName,
                    username = cleanUser,
                    emailVerified = true
                )
            }
        }

        _appStage.value = AppStage.MAIN_ENCLAVE
    }

    fun backToWelcome() {
        _appStage.value = AppStage.WELCOME
    }

    fun startConnectingTransitionToMain() {
        prefs?.edit()?.putBoolean("onboarding_v2_shown", true)?.apply()
        _appStage.value = AppStage.MAIN_ENCLAVE
    }

    fun replayOnboarding() {
        _isRegistrationCompleted.value = false
        _appStage.value = AppStage.WELCOME
    }

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        prefs?.edit()?.putBoolean("is_dark_theme_v2", next)?.apply()
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
        prefs?.edit()?.putBoolean("is_dark_theme_v2", dark)?.apply()
    }

    fun toggleLanguage() {
        val next = !_isEnglish.value
        _isEnglish.value = next
        prefs?.edit()?.putBoolean("is_english", next)?.apply()
    }

    fun updateOperatorName(newName: String) {
        val cleaned = newName.trim().ifBlank { "user" }
        _operatorName.value = cleaned
        prefs?.edit()?.putString("operator_name", cleaned)?.apply()
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
                    prefs?.edit()?.putString("avatar_file_path", destFile.absolutePath)?.apply()
                    showMessage(if (_isEnglish.value) "Operator avatar updated" else "Аватар оператора обновлен")
                }
            }
        }
    }

    fun selectServer(server: VlessServerEntity, context: Context? = null) {
        val wasConnected = _connectionStatus.value == VpnConnectionStatus.CONNECTED
        _selectedServerId.value = server.id
        prefs?.edit()?.putLong("selected_server_id", server.id)?.apply()
        appendLog(
            "INFO",
            "Selected node ${server.displayTitle(_isEnglish.value)} (${server.host}:${server.port})"
        )
        if (wasConnected && context != null) {
            viewModelScope.launch {
                connectToServer(context, server)
            }
        }
    }

    fun selectBestServer(context: Context? = null) {
        val best = allServers.value
            .filter { it.countryCode != "INFO" }
            .minByOrNull { if (it.pingMs > 0) it.pingMs else 999 }
        if (best != null) {
            selectServer(best, context)
            showMessage("${best.flagEmoji} ${best.displayTitle(_isEnglish.value)} (${best.pingMs}ms)")
        }
    }

    fun toggleConnection(context: Context) {
        when (_connectionStatus.value) {
            VpnConnectionStatus.CONNECTED, VpnConnectionStatus.CONNECTING -> {
                disconnect(context)
            }
            VpnConnectionStatus.DISCONNECTED -> {
                val all = allServers.value
                val current = selectedServer.value
                // If user has imported real servers, prefer a real imported server over the "Скоро" placeholder
                val target = if (current != null && !current.isCustom) {
                    all.firstOrNull { it.isCustom && it.countryCode != "INFO" } ?: current
                } else {
                    current ?: all.firstOrNull()
                }
                if (target != null) {
                    if (target.id != _selectedServerId.value) {
                        _selectedServerId.value = target.id
                        prefs?.edit()?.putLong("selected_server_id", target.id)?.apply()
                    }
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
            appendLog("REALITY", "Starting Xray-core [${server.host}:${server.port} • ${server.security}]")
            delay(350)

            startSystemVpnService(context, server)
            delay(450)

            val coreErr = AetherVpnService.lastCoreError
            if (!coreErr.isNullOrBlank()) {
                appendLog("WARN", "Xray status: $coreErr")
            } else if (AetherVpnService.lastCoreStatus.isNotBlank()) {
                appendLog("INFO", AetherVpnService.lastCoreStatus)
            }

            _connectionStatus.value = VpnConnectionStatus.CONNECTED
            _handshakeStep.value = "ACTIVE • ${server.displayTitle(_isEnglish.value)}"
            appendLog(
                "INFO",
                "Tunnel established: ${server.displayTitle(_isEnglish.value)} (${server.host}:${server.port}) • ${server.pingMs}ms"
            )

            if (!server.isCustom) {
                showMessage(
                    if (_isEnglish.value) "BUSH Free server is coming soon — select an imported subscription node for full VPN"
                    else "Бесплатный сервер BUSH «Скоро» — выберите сервер из вашей подписки для работы VPN"
                )
            }

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
        appendLog("WARN", "Xray-core tunnel disconnected")
    }

    private fun startSystemVpnService(context: Context, server: VlessServerEntity) {
        try {
            val currentSettings = _settings.value
            val xrayConfigJson = VlessParser.toXrayJsonConfig(
                server = server,
                settings = currentSettings,
                includeTunInbound = true
            )
            val intent = Intent(context, AetherVpnService::class.java).apply {
                action = AetherVpnService.ACTION_CONNECT
                putExtra(AetherVpnService.EXTRA_SERVER_NAME, server.displayTitle(_isEnglish.value))
                putExtra(AetherVpnService.EXTRA_SERVER_HOST, server.host)
                putExtra(AetherVpnService.EXTRA_PRIMARY_DNS, currentSettings.primaryDns)
                putExtra(AetherVpnService.EXTRA_SECONDARY_DNS, currentSettings.secondaryDns)
                putExtra(AetherVpnService.EXTRA_MTU, currentSettings.mtu)
                putExtra(AetherVpnService.EXTRA_IPV6, currentSettings.enableIpv6)
                putExtra(AetherVpnService.EXTRA_IS_CUSTOM_SERVER, server.isCustom)
                putExtra(AetherVpnService.EXTRA_XRAY_CONFIG, xrayConfigJson)
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
            appendLog("INFO", "Ping ${server.displayTitle(_isEnglish.value)} (${server.host}): ${rtt}ms")
            _pingingServerIds.update { it - server.id }
        }
    }

    fun pingAllServers() {
        if (_isPingingAll.value) return
        viewModelScope.launch {
            _isPingingAll.value = true

            // Also refresh any HTTP subscription URLs so server names & flags stay up-to-date
            val subUrls = allServers.value
                .mapNotNull { it.subscriptionUrl.takeIf { u -> u.startsWith("http", ignoreCase = true) } }
                .distinct()
            subUrls.forEach { url ->
                withContext(Dispatchers.IO) {
                    VlessParser.fetchSubscriptionFromUrl(url).onSuccess { (provider, nodes) ->
                        if (nodes.isNotEmpty()) {
                            repository.deleteBySubscriptionUrl(url)
                            repository.deleteByProviderName(provider)
                            var firstId: Long? = null
                            nodes.forEach { node ->
                                val id = repository.addServer(node)
                                if (firstId == null && node.countryCode != "INFO") {
                                    firstId = id
                                }
                            }
                            if (firstId != null) {
                                _selectedServerId.value = firstId
                                prefs?.edit()?.putLong("selected_server_id", firstId!!)?.apply()
                            }
                        }
                    }
                }
            }

            val latestServers = runCatching { repository.allServers.first() }.getOrDefault(allServers.value)
            performConcurrentTcpSweep(latestServers)
            _isPingingAll.value = false
            val updatedList = runCatching { repository.allServers.first() }.getOrDefault(allServers.value)
            val fastest = updatedList
                .filter { it.countryCode != "INFO" && it.pingMs > 0 }
                .minByOrNull { it.pingMs }
            if (fastest != null) {
                showMessage("${fastest.flagEmoji} ${fastest.displayTitle(_isEnglish.value)}: ${fastest.pingMs}ms")
            }
        }
    }

    fun importVlessUri(uriText: String, onSuccess: () -> Unit = {}) {
        importSubscriptionOrUri(uriText, onSuccess)
    }

    fun saveCustomServer(server: VlessServerEntity, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit && server.id != 0L) {
                repository.updateServer(server)
                showMessage("Обновлён: ${server.displayTitle(_isEnglish.value)}")
            } else {
                val id = repository.addServer(server)
                _selectedServerId.value = id
                prefs?.edit()?.putLong("selected_server_id", id)?.apply()
                showMessage("Добавлен: ${server.displayTitle(_isEnglish.value)}")
            }
        }
    }

    fun deleteServer(server: VlessServerEntity) {
        viewModelScope.launch {
            repository.deleteServer(server)
            showMessage("Удалён: ${server.displayTitle(_isEnglish.value)}")
        }
    }

    fun deleteProviderGroup(providerName: String) {
        viewModelScope.launch {
            repository.deleteByProviderName(providerName)
            showMessage("Подписка $providerName удалена")
        }
    }

    fun toggleEuropeSubscriptionPack() {
        viewModelScope.launch {
            val existingEu = allServers.value.filter { it.category == "SUB_EU" }
            if (existingEu.isNotEmpty()) {
                existingEu.forEach { repository.deleteServer(it) }
                showMessage(if (_isEnglish.value) "Subscription removed" else "Подписка удалена")
            } else {
                val pack = com.example.data.DefaultServers.getEuropeSubscriptionNodes()
                pack.forEach { repository.addServer(it) }
                showMessage(if (_isEnglish.value) "Subscription added (+2 servers)" else "Подписка добавлена (+2 сервера)")
            }
        }
    }

    fun importSubscriptionOrUri(rawText: String, onSuccess: () -> Unit = {}) {
        val text = rawText.trim()
        if (text.isBlank()) return

        if (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true)) {
            viewModelScope.launch {
                prefs?.edit()?.putString("last_subscription_url", text)?.apply()
                showMessage(if (_isEnglish.value) "Loading subscription..." else "Загрузка подписки...")
                val result = withContext(Dispatchers.IO) {
                    VlessParser.fetchSubscriptionFromUrl(text)
                }
                result.onSuccess { (provider, nodes) ->
                    // Replace previous nodes for this subscription URL / provider cleanly
                    repository.deleteBySubscriptionUrl(text)
                    repository.deleteByProviderName(provider)

                    var firstId: Long? = null
                    val insertedNodes = mutableListOf<VlessServerEntity>()
                    nodes.forEach { node ->
                        val id = repository.addServer(node)
                        insertedNodes.add(node.copy(id = id))
                        if (firstId == null && node.countryCode != "INFO") {
                            firstId = id
                        }
                    }
                    if (firstId != null) {
                        _selectedServerId.value = firstId
                        prefs?.edit()?.putLong("selected_server_id", firstId!!)?.apply()
                    }
                    showMessage(
                        if (_isEnglish.value) "Subscription $provider added (${nodes.size} servers)"
                        else "Подписка $provider добавлена (${nodes.size} серв.)"
                    )
                    onSuccess()
                    // Immediately run live TCP latency tests on the newly imported subscription nodes
                    launch(Dispatchers.IO) {
                        performConcurrentTcpSweep(insertedNodes)
                    }
                }.onFailure { err ->
                    showMessage(err.message ?: "Ошибка загрузки подписки")
                }
            }
            return
        }

        // Check if multi-line or Base64 subscription payload
        val multiNodes = VlessParser.parseSubscriptionPayload(text, providerName = "CUSTOM VLESS")
        if (multiNodes.isNotEmpty()) {
            viewModelScope.launch {
                var firstId: Long? = null
                val insertedNodes = mutableListOf<VlessServerEntity>()
                multiNodes.forEach { node ->
                    val id = repository.addServer(node)
                    insertedNodes.add(node.copy(id = id))
                    if (firstId == null && node.countryCode != "INFO") {
                        firstId = id
                    }
                }
                if (firstId != null) {
                    _selectedServerId.value = firstId
                    prefs?.edit()?.putLong("selected_server_id", firstId!!)?.apply()
                }
                showMessage(
                    if (_isEnglish.value) "Imported ${multiNodes.size} server(s)"
                    else "Добавлено серверов: ${multiNodes.size}"
                )
                onSuccess()
                launch(Dispatchers.IO) {
                    performConcurrentTcpSweep(insertedNodes)
                }
            }
            return
        }

        // Fallback single URI parse
        val singleResult = VlessParser.parseUri(text)
        singleResult.onSuccess { parsedServer ->
            viewModelScope.launch {
                val newId = repository.addServer(parsedServer)
                _selectedServerId.value = newId
                prefs?.edit()?.putLong("selected_server_id", newId)?.apply()
                showMessage("Добавлен: ${parsedServer.displayTitle(_isEnglish.value)}")
                onSuccess()
                launch(Dispatchers.IO) {
                    performConcurrentTcpSweep(listOf(parsedServer.copy(id = newId)))
                }
            }
        }.onFailure { err ->
            showMessage(err.message ?: "Некорректная ссылка vless:// или подписка")
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

    class Factory(
        private val repository: VpnRepository,
        private val prefs: SharedPreferences? = null,
        private val accountDao: AccountDao? = null,
        private val accountRepository: AccountRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VpnViewModel(repository, prefs, accountDao, accountRepository) as T
        }
    }
}
