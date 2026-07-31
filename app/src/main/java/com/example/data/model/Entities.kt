package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val name: String,
    val avatarUrl: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "USER"
    val bonusCoins: Long = 1000L,
    val realCoins: Long = 0L,
    val winsCount: Int = 0,
    val lossesCount: Int = 0,
    val totalBetCoins: Long = 0L,
    val totalWonCoins: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_rooms")
data class GameRoomEntity(
    @PrimaryKey val roomId: String,
    val roomName: String,
    val minPlayers: Int = 1,
    val maxPlayers: Int = 5,
    val currentHumanCount: Int = 1,
    val status: String = "WAITING", // "WAITING", "BETTING", "SPINNING", "REVEALING"
    val roundTimerSeconds: Int = 120,
    val currentRoundNumber: Int = 1,
    val roomJackpotCoins: Long = 0L,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bets")
data class BetEntity(
    @PrimaryKey(autoGenerate = true) val betId: Long = 0,
    val roomId: String,
    val roundNumber: Int,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val isBot: Boolean,
    val chosenNumber: Int, // 1..25
    val betAmount: Long,
    val walletType: String, // "BONUS", "REAL"
    val isWinner: Boolean = false,
    val payoutAmount: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "round_history")
data class RoundHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val roundNumber: Int,
    val winningNumber: Int, // 1..25
    val totalPotCoins: Long,
    val systemFeeDeducted: Long,
    val winnersCount: Int,
    val payoutPerWinner: Long,
    val rolledToRoomJackpot: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "global_jackpot")
data class GlobalJackpotEntity(
    @PrimaryKey val id: Int = 1,
    val totalGlobalJackpotCoins: Long = 50000L,
    val lastRolloverTimestamp: Long = System.currentTimeMillis(),
    val lastPayoutAmount: Long = 0L,
    val lastWinnerName: String = ""
)

@Entity(tableName = "advertisements")
data class AdvertisementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String,
    val targetUrl: String,
    val type: String, // "BANNER", "POPUP", "ANNOUNCEMENT"
    val content: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val ticketId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val subject: String,
    val chatHistoryJson: String,
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED"
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val defaultRoundTimerSeconds: Int = 120,
    val systemFeePercent: Int = 20,
    val newUserBonusCoins: Long = 1000L,
    val isMaintenanceMode: Boolean = false,
    val roomInactiveTimeoutSeconds: Int = 300
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val transactionId: String,
    val userId: String,
    val type: String, // "BONUS_GRANT", "BET_PLACED", "WIN_PAYOUT", "DEPOSIT", "WITHDRAW"
    val walletType: String, // "BONUS", "REAL"
    val amount: Long,
    val status: String = "COMPLETED", // "PENDING", "COMPLETED", "REJECTED"
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
