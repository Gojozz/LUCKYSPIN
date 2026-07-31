package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import java.util.UUID

data class RoundOutcome(
    val winningNumber: Int,
    val totalPot: Long,
    val systemFee: Long,
    val netPot: Long,
    val winners: List<BetEntity>,
    val payoutPerWinner: Long,
    val roomJackpotRollover: Long
)

class GameRepository(
    private val roomDao: GameRoomDao,
    private val betDao: BetDao,
    private val userDao: UserDao,
    private val roundHistoryDao: RoundHistoryDao,
    private val globalJackpotDao: GlobalJackpotDao,
    private val gameSettingsDao: GameSettingsDao,
    private val walletTransactionDao: WalletTransactionDao,
    private val auditLogDao: AuditLogDao
) {
    private val secureRandom = SecureRandom()

    val allRooms: Flow<List<GameRoomEntity>> = roomDao.getAllRooms()

    fun observeRoom(roomId: String): Flow<GameRoomEntity?> = roomDao.observeRoomById(roomId)

    fun observeBetsForRound(roomId: String, roundNumber: Int): Flow<List<BetEntity>> =
        betDao.getBetsForRound(roomId, roundNumber)

    fun observeUserBets(userId: String): Flow<List<BetEntity>> = betDao.getBetsForUser(userId)

    fun observeRecentRounds(): Flow<List<RoundHistoryEntity>> = roundHistoryDao.getRecentRounds()

    suspend fun getOrCreateActiveRoom(userId: String, userName: String, avatarUrl: String): GameRoomEntity {
        var room = roomDao.getRoomById("room_1")
        if (room == null) {
            val settings = gameSettingsDao.getSettingsOnce()
            room = GameRoomEntity(
                roomId = "room_1",
                roomName = "Golden Wheel Arena",
                minPlayers = 1,
                maxPlayers = 5,
                currentHumanCount = 1,
                status = "BETTING",
                roundTimerSeconds = settings?.defaultRoundTimerSeconds ?: 120,
                currentRoundNumber = 1,
                roomJackpotCoins = 2500L
            )
            roomDao.insertRoom(room)
        }
        return room
    }

    suspend fun populateSystemBots(roomId: String, currentRound: Int, humanBetCount: Int) {
        val botCountNeeded = (3..4).random() - humanBetCount
        if (botCountNeeded <= 0) return

        val botNames = listOf("VegasKing", "LuckyStar99", "SpinMaster", "JackpotHunter", "FortuneQueen", "GoldenDice")
        val botAvatars = listOf("avatar_bot1", "avatar_bot2", "avatar_bot3", "avatar_bot4")
        val betAmounts = listOf(10L, 25L, 50L, 100L, 250L, 500L)

        val existingBets = betDao.getBetsForRoundOnce(roomId, currentRound)
        val existingBotIds = existingBets.filter { it.isBot }.map { it.userId }

        val newBots = mutableListOf<BetEntity>()
        for (i in 0 until botCountNeeded) {
            val botId = "bot_${i + 1}"
            if (existingBotIds.contains(botId)) continue

            val randomNum = secureRandom.nextInt(25) + 1 // 1..25
            val randomAmount = betAmounts.random()
            newBots.add(
                BetEntity(
                    roomId = roomId,
                    roundNumber = currentRound,
                    userId = botId,
                    userName = botNames.shuffled().first(),
                    userAvatar = botAvatars.random(),
                    isBot = true,
                    chosenNumber = randomNum,
                    betAmount = randomAmount,
                    walletType = "BONUS",
                    isWinner = false,
                    payoutAmount = 0L
                )
            )
        }
        if (newBots.isNotEmpty()) {
            betDao.insertBets(newBots)
        }
    }

    suspend fun placeBet(
        roomId: String,
        roundNumber: Int,
        user: UserEntity,
        chosenNumber: Int,
        betAmount: Long,
        walletType: String // "BONUS" or "REAL"
    ): Result<Boolean> {
        if (chosenNumber !in 1..25) {
            return Result.failure(Exception("Chosen number must be between 1 and 25"))
        }
        if (betAmount <= 0) {
            return Result.failure(Exception("Bet amount must be greater than 0"))
        }

        // Deduct coins from user
        val updatedUser = if (walletType == "REAL") {
            if (user.realCoins < betAmount) return Result.failure(Exception("Insufficient Real Coins"))
            user.copy(
                realCoins = user.realCoins - betAmount,
                totalBetCoins = user.totalBetCoins + betAmount,
                lossesCount = user.lossesCount // updated at end of round
            )
        } else {
            if (user.bonusCoins < betAmount) return Result.failure(Exception("Insufficient Bonus Coins"))
            user.copy(
                bonusCoins = user.bonusCoins - betAmount,
                totalBetCoins = user.totalBetCoins + betAmount
            )
        }

        userDao.updateUser(updatedUser)

        // Insert bet
        val bet = BetEntity(
            roomId = roomId,
            roundNumber = roundNumber,
            userId = user.id,
            userName = user.name,
            userAvatar = user.avatarUrl,
            isBot = false,
            chosenNumber = chosenNumber,
            betAmount = betAmount,
            walletType = walletType
        )
        betDao.insertBet(bet)

        // Log transaction
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                transactionId = UUID.randomUUID().toString(),
                userId = user.id,
                type = "BET_PLACED",
                walletType = walletType,
                amount = -betAmount,
                status = "COMPLETED",
                note = "Placed bet on #$chosenNumber in Round $roundNumber"
            )
        )

        // Populate bots if needed so player is not alone
        val betsNow = betDao.getBetsForRoundOnce(roomId, roundNumber)
        val humanCount = betsNow.count { !it.isBot }
        populateSystemBots(roomId, roundNumber, humanCount)

        return Result.success(true)
    }

    suspend fun executeRoundSpin(roomId: String, roundNumber: Int): RoundOutcome {
        val settings = gameSettingsDao.getSettingsOnce()
        val feePercent = settings?.systemFeePercent ?: 20

        // 1. Generate cryptographically secure winning number (1..25)
        val winningNumber = secureRandom.nextInt(25) + 1

        // 2. Fetch all bets
        val bets = betDao.getBetsForRoundOnce(roomId, roundNumber)
        val totalPot = bets.sumOf { it.betAmount }

        val winners = bets.filter { it.chosenNumber == winningNumber }

        var systemFee = 0L
        var netPot = 0L
        var payoutPerWinner = 0L
        var roomJackpotRollover = 0L

        val room = roomDao.getRoomById(roomId)
        var roomJackpot = room?.roomJackpotCoins ?: 0L

        if (winners.isNotEmpty()) {
            // Take system fee (e.g. 20%)
            systemFee = (totalPot * feePercent) / 100
            netPot = totalPot - systemFee
            payoutPerWinner = netPot / winners.size

            // Pay human winners
            for (w in winners) {
                if (!w.isBot) {
                    val winnerUser = userDao.getUserById(w.userId)
                    if (winnerUser != null) {
                        val newBalance = if (w.walletType == "REAL") {
                            winnerUser.copy(
                                realCoins = winnerUser.realCoins + payoutPerWinner,
                                winsCount = winnerUser.winsCount + 1,
                                totalWonCoins = winnerUser.totalWonCoins + payoutPerWinner
                            )
                        } else {
                            winnerUser.copy(
                                bonusCoins = winnerUser.bonusCoins + payoutPerWinner,
                                winsCount = winnerUser.winsCount + 1,
                                totalWonCoins = winnerUser.totalWonCoins + payoutPerWinner
                            )
                        }
                        userDao.updateUser(newBalance)

                        walletTransactionDao.insertTransaction(
                            WalletTransactionEntity(
                                transactionId = UUID.randomUUID().toString(),
                                userId = winnerUser.id,
                                type = "WIN_PAYOUT",
                                walletType = w.walletType,
                                amount = payoutPerWinner,
                                status = "COMPLETED",
                                note = "Won Round #$roundNumber on lucky number #$winningNumber!"
                            )
                        )
                    }
                }
            }
        } else {
            // NO WINNER: No system fee taken! Entire pot becomes Room Jackpot!
            roomJackpotRollover = totalPot
            roomJackpot += totalPot
        }

        // Save round history
        roundHistoryDao.insertRound(
            RoundHistoryEntity(
                roomId = roomId,
                roundNumber = roundNumber,
                winningNumber = winningNumber,
                totalPotCoins = totalPot,
                systemFeeDeducted = systemFee,
                winnersCount = winners.size,
                payoutPerWinner = payoutPerWinner,
                rolledToRoomJackpot = roomJackpotRollover
            )
        )

        // Advance room to next round
        if (room != null) {
            roomDao.updateRoom(
                room.copy(
                    currentRoundNumber = roundNumber + 1,
                    roomJackpotCoins = roomJackpot,
                    status = "BETTING",
                    lastActiveTimestamp = System.currentTimeMillis()
                )
            )
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "ROUND_COMPLETED",
                performedBy = "SYSTEM",
                details = "Round #$roundNumber in $roomId drawn #$winningNumber. Total pot: $totalPot, Winners: ${winners.size}"
            )
        )

        return RoundOutcome(
            winningNumber = winningNumber,
            totalPot = totalPot,
            systemFee = systemFee,
            netPot = netPot,
            winners = winners,
            payoutPerWinner = payoutPerWinner,
            roomJackpotRollover = roomJackpotRollover
        )
    }

    suspend fun rolloverRoomJackpotToGlobal(roomId: String) {
        val room = roomDao.getRoomById(roomId) ?: return
        if (room.roomJackpotCoins <= 0) return

        val rolledCoins = room.roomJackpotCoins
        val globalJackpot = globalJackpotDao.getGlobalJackpotOnce() ?: GlobalJackpotEntity()

        globalJackpotDao.updateGlobalJackpot(
            globalJackpot.copy(
                totalGlobalJackpotCoins = globalJackpot.totalGlobalJackpotCoins + rolledCoins,
                lastRolloverTimestamp = System.currentTimeMillis()
            )
        )

        roomDao.updateRoom(room.copy(roomJackpotCoins = 0L))

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "JACKPOT_ROLLOVER",
                performedBy = "SYSTEM",
                details = "Rolled over $rolledCoins coins from room $roomId to Global Jackpot"
            )
        )
    }
}
