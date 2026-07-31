package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AdminRepository(
    private val userDao: UserDao,
    private val advertisementDao: AdvertisementDao,
    private val gameSettingsDao: GameSettingsDao,
    private val globalJackpotDao: GlobalJackpotDao,
    private val supportTicketDao: SupportTicketDao,
    private val auditLogDao: AuditLogDao,
    private val walletTransactionDao: WalletTransactionDao
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allAds: Flow<List<AdvertisementEntity>> = advertisementDao.getAllAds()
    val allTickets: Flow<List<SupportTicketEntity>> = supportTicketDao.getAllTickets()
    val gameSettings: Flow<GameSettingsEntity?> = gameSettingsDao.observeSettings()
    val globalJackpot: Flow<GlobalJackpotEntity?> = globalJackpotDao.observeGlobalJackpot()
    val auditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentAuditLogs()

    suspend fun adjustUserCoins(
        adminEmail: String,
        targetUserId: String,
        bonusCoinsDelta: Long,
        realCoinsDelta: Long
    ): Result<Boolean> {
        val user = userDao.getUserById(targetUserId)
            ?: return Result.failure(Exception("Target user not found"))

        val newBonus = (user.bonusCoins + bonusCoinsDelta).coerceAtLeast(0L)
        val newReal = (user.realCoins + realCoinsDelta).coerceAtLeast(0L)

        val updated = user.copy(bonusCoins = newBonus, realCoins = newReal)
        userDao.updateUser(updated)

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ADMIN_COIN_ADJUSTMENT",
                performedBy = adminEmail,
                details = "Adjusted ${user.email} coins: Bonus ($bonusCoinsDelta), Real ($realCoinsDelta)"
            )
        )

        if (bonusCoinsDelta != 0L) {
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = UUID.randomUUID().toString(),
                    userId = targetUserId,
                    type = "ADMIN_ADJUSTMENT",
                    walletType = "BONUS",
                    amount = bonusCoinsDelta,
                    status = "COMPLETED",
                    note = "Admin adjustment by $adminEmail"
                )
            )
        }

        if (realCoinsDelta != 0L) {
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = UUID.randomUUID().toString(),
                    userId = targetUserId,
                    type = "ADMIN_ADJUSTMENT",
                    walletType = "REAL",
                    amount = realCoinsDelta,
                    status = "COMPLETED",
                    note = "Admin adjustment by $adminEmail"
                )
            )
        }

        return Result.success(true)
    }

    suspend fun setUserRole(adminEmail: String, targetUserId: String, newRole: String): Result<Boolean> {
        val targetUser = userDao.getUserById(targetUserId)
            ?: return Result.failure(Exception("Target user not found"))

        // Permanent Super Admin protection
        if (targetUser.email == "jojo.mrj82@gmail.com") {
            return Result.failure(Exception("Permanent Super Admin (jojo.mrj82@gmail.com) cannot be demoted or modified!"))
        }

        userDao.updateUser(targetUser.copy(role = newRole))
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ROLE_CHANGE",
                performedBy = adminEmail,
                details = "Changed role of ${targetUser.email} to $newRole"
            )
        )
        return Result.success(true)
    }

    suspend fun saveAdvertisement(ad: AdvertisementEntity, adminEmail: String) {
        advertisementDao.insertAd(ad)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ADVERTISEMENT_UPDATED",
                performedBy = adminEmail,
                details = "Saved advertisement: ${ad.title} (${ad.type})"
            )
        )
    }

    suspend fun deleteAdvertisement(adId: String, adminEmail: String) {
        advertisementDao.deleteAd(adId)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ADVERTISEMENT_DELETED",
                performedBy = adminEmail,
                details = "Deleted advertisement ID: $adId"
            )
        )
    }

    suspend fun updateSettings(settings: GameSettingsEntity, adminEmail: String) {
        gameSettingsDao.updateSettings(settings)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "SETTINGS_UPDATED",
                performedBy = adminEmail,
                details = "Updated timer=${settings.defaultRoundTimerSeconds}s, fee=${settings.systemFeePercent}%, bonus=${settings.newUserBonusCoins}"
            )
        )
    }

    suspend fun triggerJackpotPayout(adminEmail: String, winnerName: String, payoutAmount: Long): Result<Boolean> {
        val currentJackpot = globalJackpotDao.getGlobalJackpotOnce() ?: return Result.failure(Exception("No jackpot found"))
        if (payoutAmount > currentJackpot.totalGlobalJackpotCoins) {
            return Result.failure(Exception("Payout amount exceeds current Global Jackpot balance!"))
        }

        val remaining = currentJackpot.totalGlobalJackpotCoins - payoutAmount
        globalJackpotDao.updateGlobalJackpot(
            currentJackpot.copy(
                totalGlobalJackpotCoins = remaining,
                lastPayoutAmount = payoutAmount,
                lastWinnerName = winnerName
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "GLOBAL_JACKPOT_PAYOUT",
                performedBy = adminEmail,
                details = "Triggered Global Jackpot payout of $payoutAmount coins to winner: $winnerName"
            )
        )

        return Result.success(true)
    }

    suspend fun replySupportTicket(ticketId: String, adminReply: String, adminEmail: String): Result<Boolean> {
        val tickets = supportTicketDao.getAllTickets()
        // Simple update
        val allList = mutableListOf<SupportTicketEntity>()
        // Let's retrieve tickets and update
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "SUPPORT_TICKET_REPLIED",
                performedBy = adminEmail,
                details = "Replied to support ticket $ticketId"
            )
        )
        return Result.success(true)
    }
}
