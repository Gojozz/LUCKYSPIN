package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.data.repository.RoundOutcome
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val gameRepository: GameRepository) : ViewModel() {

    private val _currentRoom = MutableStateFlow<GameRoomEntity?>(null)
    val currentRoom: StateFlow<GameRoomEntity?> = _currentRoom

    private val _bets = MutableStateFlow<List<BetEntity>>(emptyList())
    val bets: StateFlow<List<BetEntity>> = _bets

    private val _timerSeconds = MutableStateFlow(120)
    val timerSeconds: StateFlow<Int> = _timerSeconds

    private val _selectedNumber = MutableStateFlow<Int?>(null)
    val selectedNumber: StateFlow<Int?> = _selectedNumber

    private val _betAmount = MutableStateFlow(50L)
    val betAmount: StateFlow<Long> = _betAmount

    private val _walletType = MutableStateFlow("BONUS") // "BONUS" or "REAL"
    val walletType: StateFlow<String> = _walletType

    private val _isBetLocked = MutableStateFlow(false)
    val isBetLocked: StateFlow<Boolean> = _isBetLocked

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning

    private val _winningNumber = MutableStateFlow<Int?>(null)
    val winningNumber: StateFlow<Int?> = _winningNumber

    private val _lastOutcome = MutableStateFlow<RoundOutcome?>(null)
    val lastOutcome: StateFlow<RoundOutcome?> = _lastOutcome

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var timerJob: Job? = null

    fun enterRoom(userId: String, userName: String, avatarUrl: String) {
        viewModelScope.launch {
            val room = gameRepository.getOrCreateActiveRoom(userId, userName, avatarUrl)
            _currentRoom.value = room
            _timerSeconds.value = room.roundTimerSeconds

            // Observe bets for current round
            launch {
                gameRepository.observeBetsForRound(room.roomId, room.currentRoundNumber).collect { betList ->
                    _bets.value = betList
                    // Check if current user placed bet
                    val myBet = betList.find { it.userId == userId }
                    if (myBet != null) {
                        _selectedNumber.value = myBet.chosenNumber
                        _isBetLocked.value = true
                    }
                }
            }

            startTimer()
        }
    }

    fun selectNumber(num: Int) {
        if (_isBetLocked.value || _isSpinning.value) return
        _selectedNumber.value = num
    }

    fun setBetAmount(amount: Long) {
        if (_isBetLocked.value || _isSpinning.value) return
        _betAmount.value = amount
    }

    fun setWalletType(type: String) {
        if (_isBetLocked.value || _isSpinning.value) return
        _walletType.value = type
    }

    fun placeBet(user: UserEntity) {
        val num = _selectedNumber.value
        if (num == null) {
            _message.value = "Please select a lucky number (1-25)"
            return
        }

        val room = _currentRoom.value ?: return

        viewModelScope.launch {
            val res = gameRepository.placeBet(
                roomId = room.roomId,
                roundNumber = room.currentRoundNumber,
                user = user,
                chosenNumber = num,
                betAmount = _betAmount.value,
                walletType = _walletType.value
            )

            if (res.isSuccess) {
                _isBetLocked.value = true
                _message.value = "Bet confirmed on #$num!"
            } else {
                _message.value = res.exceptionOrNull()?.message ?: "Failed to place bet"
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1000)
                _timerSeconds.value -= 1
            }
            // Timer expired -> Lock bets and execute spin!
            triggerSpin()
        }
    }

    fun setFastTimerPreview(seconds: Int) {
        _timerSeconds.value = seconds
        startTimer()
    }

    private fun triggerSpin() {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            _isSpinning.value = true
            val outcome = gameRepository.executeRoundSpin(room.roomId, room.currentRoundNumber)
            _winningNumber.value = outcome.winningNumber
            _lastOutcome.value = outcome

            // Wait for wheel animation duration (4000ms)
            delay(4200)
            _isSpinning.value = false

            // Show outcome message
            if (outcome.winners.isNotEmpty()) {
                _message.value = "Round Ended! Winning Number: #${outcome.winningNumber}. Winners share ${outcome.netPot} coins!"
            } else {
                _message.value = "No winners this round! ${outcome.totalPot} coins rolled to Room Jackpot!"
            }

            // Delay before starting next round
            delay(5000)
            resetForNextRound(room.roomId, room.currentRoundNumber + 1)
        }
    }

    private fun resetForNextRound(roomId: String, nextRoundNumber: Int) {
        _selectedNumber.value = null
        _isBetLocked.value = false
        _winningNumber.value = null
        _lastOutcome.value = null
        _currentRoom.value = _currentRoom.value?.copy(currentRoundNumber = nextRoundNumber)
        _timerSeconds.value = _currentRoom.value?.roundTimerSeconds ?: 120

        startTimer()
    }

    fun clearMessage() {
        _message.value = null
    }
}
