package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        GameRoomEntity::class,
        BetEntity::class,
        RoundHistoryEntity::class,
        GlobalJackpotEntity::class,
        AdvertisementEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class,
        GameSettingsEntity::class,
        WalletTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun gameRoomDao(): GameRoomDao
    abstract fun betDao(): BetDao
    abstract fun roundHistoryDao(): RoundHistoryDao
    abstract fun globalJackpotDao(): GlobalJackpotDao
    abstract fun advertisementDao(): AdvertisementDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun walletTransactionDao(): WalletTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lucky_spin_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default database state in coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedDefaults(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaults(db: AppDatabase) {
            // Seed Super Admin
            val superAdmin = UserEntity(
                id = "super_admin_jojo",
                email = "jojo.mrj82@gmail.com",
                passwordHash = "admin123", // SHA-256 / Simple hash simulation
                name = "Super Admin Jojo",
                avatarUrl = "avatar_crown",
                role = "SUPER_ADMIN",
                bonusCoins = 50000L,
                realCoins = 10000L,
                winsCount = 10,
                lossesCount = 2,
                totalBetCoins = 5000L,
                totalWonCoins = 25000L
            )
            db.userDao().insertUser(superAdmin)

            // Seed Settings
            db.gameSettingsDao().updateSettings(
                GameSettingsEntity(
                    id = 1,
                    defaultRoundTimerSeconds = 120,
                    systemFeePercent = 20,
                    newUserBonusCoins = 1000L,
                    isMaintenanceMode = false
                )
            )

            // Seed Global Jackpot
            db.globalJackpotDao().updateGlobalJackpot(
                GlobalJackpotEntity(
                    id = 1,
                    totalGlobalJackpotCoins = 100000L,
                    lastRolloverTimestamp = System.currentTimeMillis()
                )
            )

            // Seed Default Rooms
            val room1 = GameRoomEntity(
                roomId = "room_1",
                roomName = "Golden Wheel Arena",
                minPlayers = 1,
                maxPlayers = 5,
                currentHumanCount = 1,
                status = "WAITING",
                roundTimerSeconds = 120,
                currentRoundNumber = 1,
                roomJackpotCoins = 5000L
            )
            val room2 = GameRoomEntity(
                roomId = "room_2",
                roomName = "High Rollers Lounge",
                minPlayers = 1,
                maxPlayers = 5,
                currentHumanCount = 1,
                status = "WAITING",
                roundTimerSeconds = 120,
                currentRoundNumber = 1,
                roomJackpotCoins = 15000L
            )
            db.gameRoomDao().insertRoom(room1)
            db.gameRoomDao().insertRoom(room2)

            // Seed Advertisements
            db.advertisementDao().insertAd(
                AdvertisementEntity(
                    id = "ad_1",
                    title = "Mega Jackpot Tournament",
                    imageUrl = "img_ad_banner_1",
                    targetUrl = "https://luckyspin.com/tournament",
                    type = "BANNER",
                    content = "Spin today to enter the 100,000 Coin Global Jackpot draw!",
                    isActive = true
                )
            )
            db.advertisementDao().insertAd(
                AdvertisementEntity(
                    id = "ad_2",
                    title = "Daily Login Reward",
                    imageUrl = "img_ad_banner_2",
                    targetUrl = "https://luckyspin.com/daily",
                    type = "ANNOUNCEMENT",
                    content = "Claim 1,000 Bonus Coins every 24 hours. Keep your streak alive!",
                    isActive = true
                )
            )

            // Audit log
            db.auditLogDao().insertLog(
                AuditLogEntity(
                    action = "SYSTEM_INITIALIZED",
                    performedBy = "SYSTEM",
                    details = "Lucky Spin Multiplayer database initialized with Super Admin jojo.mrj82@gmail.com"
                )
            )
        }
    }
}
