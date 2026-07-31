package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface GameRoomDao {
    @Query("SELECT * FROM game_rooms ORDER BY lastActiveTimestamp DESC")
    fun getAllRooms(): Flow<List<GameRoomEntity>>

    @Query("SELECT * FROM game_rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: String): GameRoomEntity?

    @Query("SELECT * FROM game_rooms WHERE roomId = :roomId LIMIT 1")
    fun observeRoomById(roomId: String): Flow<GameRoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: GameRoomEntity)

    @Update
    suspend fun updateRoom(room: GameRoomEntity)
}

@Dao
interface BetDao {
    @Query("SELECT * FROM bets WHERE roomId = :roomId AND roundNumber = :roundNumber")
    fun getBetsForRound(roomId: String, roundNumber: Int): Flow<List<BetEntity>>

    @Query("SELECT * FROM bets WHERE roomId = :roomId AND roundNumber = :roundNumber")
    suspend fun getBetsForRoundOnce(roomId: String, roundNumber: Int): List<BetEntity>

    @Query("SELECT * FROM bets WHERE userId = :userId ORDER BY timestamp DESC LIMIT 50")
    fun getBetsForUser(userId: String): Flow<List<BetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBets(bets: List<BetEntity>)

    @Query("DELETE FROM bets WHERE roomId = :roomId AND roundNumber = :roundNumber")
    suspend fun clearBetsForRound(roomId: String, roundNumber: Int)
}

@Dao
interface RoundHistoryDao {
    @Query("SELECT * FROM round_history ORDER BY timestamp DESC LIMIT 30")
    fun getRecentRounds(): Flow<List<RoundHistoryEntity>>

    @Query("SELECT * FROM round_history WHERE roomId = :roomId ORDER BY timestamp DESC LIMIT 20")
    fun getRoundsForRoom(roomId: String): Flow<List<RoundHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(history: RoundHistoryEntity)
}

@Dao
interface GlobalJackpotDao {
    @Query("SELECT * FROM global_jackpot WHERE id = 1 LIMIT 1")
    fun observeGlobalJackpot(): Flow<GlobalJackpotEntity?>

    @Query("SELECT * FROM global_jackpot WHERE id = 1 LIMIT 1")
    suspend fun getGlobalJackpotOnce(): GlobalJackpotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGlobalJackpot(jackpot: GlobalJackpotEntity)
}

@Dao
interface AdvertisementDao {
    @Query("SELECT * FROM advertisements WHERE isActive = 1")
    fun getActiveAds(): Flow<List<AdvertisementEntity>>

    @Query("SELECT * FROM advertisements ORDER BY createdAt DESC")
    fun getAllAds(): Flow<List<AdvertisementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdvertisementEntity)

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAd(id: String)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface GameSettingsDao {
    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<GameSettingsEntity?>

    @Query("SELECT * FROM game_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): GameSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: GameSettingsEntity)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC LIMIT 100")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity)

    @Update
    suspend fun updateTransaction(tx: WalletTransactionEntity)
}
