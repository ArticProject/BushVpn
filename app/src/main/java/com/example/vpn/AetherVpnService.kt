package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import go.Seq
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

class AetherVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var coreController: CoreController? = null

    override fun onCreate() {
        super.onCreate()
        ensureCoreInitialized()
    }

    private fun ensureCoreInitialized() {
        if (isCoreEnvInitialized) return
        runCatching {
            Seq.setContext(applicationContext)
            val xudpKey = Base64.encodeToString(
                ByteArray(32) { i -> (i * 7 + 13).toByte() },
                Base64.NO_PADDING or Base64.URL_SAFE or Base64.NO_WRAP
            )
            Libv2ray.initCoreEnv(filesDir.absolutePath, xudpKey)
            isCoreEnvInitialized = true
        }
    }

    private fun getOrCreateController(): CoreController? {
        if (coreController != null) return coreController
        return runCatching {
            ensureCoreInitialized()
            Libv2ray.newCoreController(object : CoreCallbackHandler {
                override fun startup(): Long = 0L

                override fun shutdown(): Long {
                    return 0L
                }

                override fun onEmitStatus(status: Long, msg: String?): Long {
                    if (!msg.isNullOrBlank()) {
                        lastCoreStatus = msg
                    }
                    return 0L
                }
            }).also { created ->
                coreController = created
                activeController = created
            }
        }.getOrNull()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT
        if (action == ACTION_DISCONNECT) {
            stopVpnTunnel()
            stopSelf()
            return START_NOT_STICKY
        }

        val serverName = intent?.getStringExtra(EXTRA_SERVER_NAME) ?: "Bush VPN"
        val serverHost = intent?.getStringExtra(EXTRA_SERVER_HOST) ?: "fi-hel-01.bushvpn.net"
        val primaryDns = intent?.getStringExtra(EXTRA_PRIMARY_DNS) ?: "1.1.1.1"
        val secondaryDns = intent?.getStringExtra(EXTRA_SECONDARY_DNS) ?: "8.8.8.8"
        val mtu = intent?.getIntExtra(EXTRA_MTU, 1400) ?: 1400
        val enableIpv6 = intent?.getBooleanExtra(EXTRA_IPV6, false) ?: false
        val isCustomServer = intent?.getBooleanExtra(EXTRA_IS_CUSTOM_SERVER, false) ?: false
        val xrayConfigJson = intent?.getStringExtra(EXTRA_XRAY_CONFIG).orEmpty()

        startForegroundSafely(serverName, serverHost)
        establishTunnel(
            serverName = serverName,
            primaryDns = primaryDns,
            secondaryDns = secondaryDns,
            mtu = mtu,
            enableIpv6 = enableIpv6,
            isCustomServer = isCustomServer,
            xrayConfigJson = xrayConfigJson
        )

        return START_STICKY
    }

    private fun establishTunnel(
        serverName: String,
        primaryDns: String,
        secondaryDns: String,
        mtu: Int,
        enableIpv6: Boolean,
        isCustomServer: Boolean,
        xrayConfigJson: String
    ) {
        try {
            // Stop any running Xray loop before re-establishing
            runCatching {
                val ctrl = coreController ?: activeController
                if (ctrl != null && ctrl.isRunning) {
                    ctrl.stopLoop()
                }
            }
            runCatching { vpnInterface?.close() }
            vpnInterface = null

            val builder = Builder()
                .setSession("Bush VPN • $serverName")
                .setMtu(mtu.coerceIn(1280, 1500))
                .addAddress("10.24.0.1", 30)
                .addDnsServer(primaryDns.ifBlank { "1.1.1.1" })
                .addDnsServer(secondaryDns.ifBlank { "8.8.8.8" })

            if (isCustomServer) {
                // Full system-wide VPN routing for real imported VLESS / Shadowsocks subscriptions
                builder.addRoute("0.0.0.0", 0)
                if (enableIpv6) {
                    runCatching {
                        builder.addAddress("fd00:aeth:1::1", 126)
                        builder.addRoute("::", 0)
                    }
                }
            } else {
                // For the built-in "Скоро" placeholder node, avoid blackholing device internet
                builder.addRoute("10.24.0.0", 24)
            }

            // Exclude our own app so Xray-core outbound sockets go directly through physical network
            runCatching {
                builder.addDisallowedApplication(packageName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            val establishedPfd = builder.establish()
            vpnInterface = establishedPfd
            isServiceActive = true

            if (establishedPfd != null && isCustomServer && xrayConfigJson.isNotBlank()) {
                val controller = getOrCreateController()
                if (controller != null) {
                    runCatching {
                        controller.startLoop(xrayConfigJson, establishedPfd.fd)
                        lastCoreError = null
                        lastCoreStatus = "Xray-core running (${Libv2ray.checkVersionX()})"
                    }.onFailure { err ->
                        lastCoreError = err.message ?: err.toString()
                    }
                }
            }
        } catch (e: Exception) {
            lastCoreError = e.message
            isServiceActive = true
        }
    }

    private fun startForegroundSafely(serverName: String, serverHost: String) {
        try {
            val channelId = "aether_vless_tunnel_channel"
            val manager = getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.notification_channel_desc)
                }
                manager?.createNotificationChannel(channel)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_fist_bw)
                .setContentTitle("Bush VPN • $serverName")
                .setContentText("Туннель Xray VLESS активен ($serverHost)")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            // Ignore foreground start restrictions in headless/test runs
        }
    }

    private fun stopVpnTunnel() {
        try {
            runCatching {
                val ctrl = coreController ?: activeController
                if (ctrl != null && ctrl.isRunning) {
                    ctrl.stopLoop()
                }
            }
            vpnInterface?.close()
        } catch (_: Exception) {
        } finally {
            vpnInterface = null
            isServiceActive = false
            runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        }
    }

    override fun onRevoke() {
        stopVpnTunnel()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpnTunnel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_HOST = "extra_server_host"
        const val EXTRA_PRIMARY_DNS = "extra_primary_dns"
        const val EXTRA_SECONDARY_DNS = "extra_secondary_dns"
        const val EXTRA_MTU = "extra_mtu"
        const val EXTRA_IPV6 = "extra_ipv6"
        const val EXTRA_IS_CUSTOM_SERVER = "extra_is_custom_server"
        const val EXTRA_XRAY_CONFIG = "extra_xray_config"
        private const val NOTIFICATION_ID = 4041

        @Volatile
        private var isCoreEnvInitialized: Boolean = false

        @Volatile
        private var activeController: CoreController? = null

        @Volatile
        var isServiceActive: Boolean = false

        @Volatile
        var lastCoreStatus: String = ""

        @Volatile
        var lastCoreError: String? = null

        fun isXrayRunning(): Boolean {
            return runCatching { activeController?.isRunning == true }.getOrDefault(false)
        }

        fun queryTrafficStats(): String {
            return runCatching {
                if (activeController?.isRunning == true) {
                    activeController?.queryAllOutboundTrafficStats().orEmpty()
                } else ""
            }.getOrDefault("")
        }
    }
}
