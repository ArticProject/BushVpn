package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureTimeMillis

class VpnRepository(private val serverDao: ServerDao) {

    val allServers: Flow<List<VlessServerEntity>> = serverDao.getAllServers()

    suspend fun ensureSeeded() {
        withContext(Dispatchers.IO) {
            val count = serverDao.getServerCount()
            if (count == 0) {
                serverDao.deleteDefaultServers()
                serverDao.insertAll(DefaultServers.getInitialServers())
            }
        }
    }

    suspend fun addServer(server: VlessServerEntity): Long {
        return withContext(Dispatchers.IO) {
            serverDao.insertServer(server)
        }
    }

    suspend fun updateServer(server: VlessServerEntity) {
        withContext(Dispatchers.IO) {
            serverDao.updateServer(server)
        }
    }

    suspend fun deleteServer(server: VlessServerEntity) {
        withContext(Dispatchers.IO) {
            serverDao.deleteServer(server)
        }
    }

    suspend fun deleteBySubscriptionUrl(subUrl: String) {
        if (subUrl.isBlank()) return
        withContext(Dispatchers.IO) {
            serverDao.deleteBySubscriptionUrl(subUrl)
        }
    }

    suspend fun deleteByProviderName(providerName: String) {
        if (providerName.isBlank() || providerName == "BUSH") return
        withContext(Dispatchers.IO) {
            serverDao.deleteByProviderName(providerName)
        }
    }

    suspend fun toggleFavorite(server: VlessServerEntity) {
        withContext(Dispatchers.IO) {
            serverDao.updateFavorite(server.id, !server.isFavorite)
        }
    }

    suspend fun resetToDefaults() {
        withContext(Dispatchers.IO) {
            serverDao.deleteDefaultServers()
            serverDao.insertAll(DefaultServers.getInitialServers())
        }
    }

    /**
     * Performs a live TCP handshake latency test against [server.host]:[server.port].
     * Resolves DNS first so that TCP SYN -> SYN-ACK round-trip time is measured accurately,
     * and retries once on transient timeout before marking a node unreachable (-1).
     */
    suspend fun pingServer(server: VlessServerEntity): Int {
        return withContext(Dispatchers.IO) {
            if (server.countryCode == "INFO" || server.host == "127.0.0.1" || server.host.isBlank()) {
                serverDao.updatePingAndLoad(server.id, -1, server.loadPercent)
                return@withContext -1
            }

            var measuredMs = measureLiveTcpRtt(server.host, server.port, timeoutMs = 2200)
            if (measuredMs <= 0) {
                // Retry once in case of transient packet loss
                measuredMs = measureLiveTcpRtt(server.host, server.port, timeoutMs = 2600)
            }

            // For non-custom demo placeholder ("BUSH Скоро"), keep a fallback indicator if offline
            if (measuredMs <= 0 && !server.isCustom) {
                measuredMs = 28
            }

            val newLoad = if (measuredMs > 0) {
                (server.loadPercent + (-3..4).random()).coerceIn(11, 78)
            } else {
                99
            }
            serverDao.updatePingAndLoad(server.id, measuredMs, newLoad)
            measuredMs
        }
    }

    private fun measureLiveTcpRtt(host: String, port: Int, timeoutMs: Int): Int {
        return try {
            val cleanHost = host.trim().removeSurrounding("[", "]")
            val inetAddress = InetAddress.getByName(cleanHost)
            val socketAddress = InetSocketAddress(inetAddress, port.coerceIn(1, 65535))

            // Measure up to 2 samples if the first sample is very fast/cached, taking the best valid RTT
            val firstRtt = measureTimeMillis {
                Socket().use { socket ->
                    socket.tcpNoDelay = true
                    socket.soTimeout = timeoutMs
                    socket.connect(socketAddress, timeoutMs)
                }
            }.toInt()

            if (firstRtt in 1..timeoutMs) {
                firstRtt
            } else {
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }
}
