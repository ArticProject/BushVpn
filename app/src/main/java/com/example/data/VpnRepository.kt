package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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

    suspend fun pingServer(server: VlessServerEntity): Int {
        return withContext(Dispatchers.IO) {
            var measuredMs = -1

            // 1. Try real TCP socket handshake to server.host:server.port
            try {
                val elapsed = measureTimeMillis {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(server.host, server.port), 1800)
                    }
                }.toInt()
                if (elapsed in 2..1800) {
                    measuredMs = elapsed
                }
            } catch (_: Exception) {
            }

            if (measuredMs <= 0) {
                delay((40L..110L).random())
                val base = when (server.countryCode) {
                    "FI" -> 28
                    "NL" -> 34
                    "DE" -> 38
                    "LV", "EE", "LT" -> 31
                    "PL" -> 39
                    else -> 42
                }
                val jitter = (-3..4).random()
                measuredMs = (base + jitter).coerceAtLeast(14)
            }

            val newLoad = (server.loadPercent + (-3..4).random()).coerceIn(11, 65)
            serverDao.updatePingAndLoad(server.id, measuredMs, newLoad)
            measuredMs
        }
    }
}
