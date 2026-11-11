package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val name: String,
    val username: String,
    val passwordHash: String,
    val emailVerified: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "email_verification_codes")
data class EmailVerificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val code: String,
    val verified: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAtMillis DESC")
    fun observeAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationCode(entry: EmailVerificationEntity): Long

    @Query("SELECT * FROM email_verification_codes WHERE email = :email ORDER BY createdAtMillis DESC LIMIT 1")
    suspend fun getLatestCodeForEmail(email: String): EmailVerificationEntity?

    @Query("UPDATE email_verification_codes SET verified = 1 WHERE id = :id")
    suspend fun markCodeVerified(id: Long)

    @Query("SELECT * FROM email_verification_codes ORDER BY createdAtMillis DESC LIMIT 20")
    fun observeRecentCodes(): Flow<List<EmailVerificationEntity>>
}
