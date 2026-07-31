package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    val allUsers = adminRepository.allUsers
    val allAds = adminRepository.allAds
    val allTickets = adminRepository.allTickets
    val gameSettings = adminRepository.gameSettings
    val globalJackpot = adminRepository.globalJackpot
    val auditLogs = adminRepository.auditLogs

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage

    fun adjustUserCoins(adminEmail: String, targetUserId: String, bonusDelta: Long, realDelta: Long) {
        viewModelScope.launch {
            val res = adminRepository.adjustUserCoins(adminEmail, targetUserId, bonusDelta, realDelta)
            if (res.isSuccess) {
                _adminMessage.value = "Successfully adjusted coins for user!"
            } else {
                _adminMessage.value = res.exceptionOrNull()?.message ?: "Failed coin adjustment"
            }
        }
    }

    fun setUserRole(adminEmail: String, targetUserId: String, newRole: String) {
        viewModelScope.launch {
            val res = adminRepository.setUserRole(adminEmail, targetUserId, newRole)
            if (res.isSuccess) {
                _adminMessage.value = "User role updated to $newRole"
            } else {
                _adminMessage.value = res.exceptionOrNull()?.message ?: "Failed role update"
            }
        }
    }

    fun updateGameSettings(adminEmail: String, roundTimer: Int, feePercent: Int, bonusCoins: Long) {
        viewModelScope.launch {
            adminRepository.updateSettings(
                GameSettingsEntity(
                    id = 1,
                    defaultRoundTimerSeconds = roundTimer,
                    systemFeePercent = feePercent,
                    newUserBonusCoins = bonusCoins
                ),
                adminEmail
            )
            _adminMessage.value = "Game settings updated successfully!"
        }
    }

    fun saveAd(adminEmail: String, title: String, content: String, type: String) {
        viewModelScope.launch {
            val ad = AdvertisementEntity(
                id = "ad_${System.currentTimeMillis()}",
                title = title,
                imageUrl = "img_ad_banner_1",
                targetUrl = "https://luckyspin.com",
                type = type,
                content = content,
                isActive = true
            )
            adminRepository.saveAdvertisement(ad, adminEmail)
            _adminMessage.value = "New advertisement saved!"
        }
    }

    fun deleteAd(adminEmail: String, adId: String) {
        viewModelScope.launch {
            adminRepository.deleteAdvertisement(adId, adminEmail)
            _adminMessage.value = "Advertisement deleted."
        }
    }

    fun triggerJackpotPayout(adminEmail: String, winnerName: String, amount: Long) {
        viewModelScope.launch {
            val res = adminRepository.triggerJackpotPayout(adminEmail, winnerName, amount)
            if (res.isSuccess) {
                _adminMessage.value = "Triggered Global Jackpot payout of $amount coins to $winnerName!"
            } else {
                _adminMessage.value = res.exceptionOrNull()?.message ?: "Jackpot payout failed"
            }
        }
    }

    fun clearMessage() {
        _adminMessage.value = null
    }
}
