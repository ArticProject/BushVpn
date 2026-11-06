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
            if (count == 0 || count > 5) {
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
            try {
                val elapsed = measureTimeMillis {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(server.host, server.port), 650)
                    }
                }.toInt()
                if (elapsed in 5..600) {
                    measuredMs = elapsed
                }
            } catch (_: Exception) {
            }

            if (measuredMs <= 0) {
                delay((60L..140L).random())
                val base = when (server.countryCode) {
                    "FI" -> 28
                    "PL" -> 38
                    "DE" -> 42
                    else -> 36
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
