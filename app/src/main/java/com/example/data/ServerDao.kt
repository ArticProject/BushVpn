package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM vless_servers ORDER BY id ASC")
    fun getAllServers(): Flow<List<VlessServerEntity>>

    @Query("SELECT COUNT(*) FROM vless_servers")
    suspend fun getServerCount(): Int

    @Query("DELETE FROM vless_servers WHERE isCustom = 0")
    suspend fun deleteDefaultServers()

    @Query("DELETE FROM vless_servers WHERE subscriptionUrl = :subUrl AND isCustom = 1")
    suspend fun deleteBySubscriptionUrl(subUrl: String)

    @Query("DELETE FROM vless_servers WHERE providerName = :providerName AND isCustom = 1")
    suspend fun deleteByProviderName(providerName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: VlessServerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(servers: List<VlessServerEntity>)

    @Update
    suspend fun updateServer(server: VlessServerEntity)

    @Delete
    suspend fun deleteServer(server: VlessServerEntity)

    @Query("UPDATE vless_servers SET pingMs = :pingMs, loadPercent = :loadPercent WHERE id = :id")
    suspend fun updatePingAndLoad(id: Long, pingMs: Int, loadPercent: Int)

    @Query("UPDATE vless_servers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
