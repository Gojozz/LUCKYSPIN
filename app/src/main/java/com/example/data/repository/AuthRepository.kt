package com.example.data.repository

import com.example.data.local.AuditLogDao
import com.example.data.local.GameSettingsDao
import com.example.data.local.UserDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val gameSettingsDao: GameSettingsDao,
    private val auditLogDao: AuditLogDao
) {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("User not found with email $email"))

        val hashedInput = hashPassword(password)
        if (user.passwordHash != hashedInput && user.passwordHash != password) {
            return Result.failure(Exception("Invalid password"))
        }

        _currentUser.value = user
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "USER_LOGIN",
                performedBy = user.email,
                details = "User ${user.name} logged in successfully"
            )
        )
        return Result.success(user)
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        avatarUrl: String = "avatar_gold"
    ): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("Email is already registered"))
        }

        val settings = gameSettingsDao.getSettingsOnce()
        val bonusGrant = settings?.newUserBonusCoins ?: 1000L

        // Determine role: if jojo.mrj82@gmail.com -> SUPER_ADMIN, else USER
        val role = if (trimmedEmail == "jojo.mrj82@gmail.com") "SUPER_ADMIN" else "USER"

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            passwordHash = hashPassword(password),
            name = name.ifBlank { "Player_${(1000..9999).random()}" },
            avatarUrl = avatarUrl,
            role = role,
            bonusCoins = bonusGrant,
            realCoins = if (role == "SUPER_ADMIN") 5000L else 0L
        )

        userDao.insertUser(newUser)
        _currentUser.value = newUser

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "USER_REGISTERED",
                performedBy = newUser.email,
                details = "Registered new account with role $role and $bonusGrant Bonus Coins"
            )
        )
        return Result.success(newUser)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("User email not found"))

        val updated = user.copy(passwordHash = hashPassword(newPassword))
        userDao.updateUser(updated)
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = updated
        }
        return Result.success(true)
    }

    fun logout() {
        val user = _currentUser.value
        _currentUser.value = null
    }

    fun observeUser(userId: String): Flow<UserEntity?> = userDao.observeUserById(userId)

    suspend fun refreshCurrentUser() {
        _currentUser.value?.let { current ->
            val fresh = userDao.getUserById(current.id)
            if (fresh != null) {
                _currentUser.value = fresh
            }
        }
    }
}
