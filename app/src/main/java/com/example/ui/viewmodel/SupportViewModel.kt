package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SupportTicketDao
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UserEntity
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val sender: String, // "user" or "spinbot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SupportViewModel(private val ticketDao: SupportTicketDao) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "spinbot",
                text = "Hello! I am SpinBot, your 24/7 Lucky Spin AI Assistant. Ask me anything about betting on 1-25 numbers, round timers, 20% system fees, wallet coins, or jackpots!"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking

    private val _ticketMessage = MutableStateFlow<String?>(null)
    val ticketMessage: StateFlow<String?> = _ticketMessage

    fun sendMessage(userText: String, user: UserEntity?) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = "user", text = userText)
        val updatedList = _messages.value + userMsg
        _messages.value = updatedList

        viewModelScope.launch {
            _isAiThinking.value = true

            val chatHistory = updatedList.map { it.sender to it.text }
            val aiReply = GeminiClient.getAiSupportReply(userText, chatHistory)

            _isAiThinking.value = false
            _messages.value = _messages.value + ChatMessage(sender = "spinbot", text = aiReply)
        }
    }

    fun createSupportTicket(subject: String, user: UserEntity?) {
        if (user == null) return
        viewModelScope.launch {
            val historyJson = _messages.value.joinToString("\n") { "${it.sender}: ${it.text}" }
            val ticket = SupportTicketEntity(
                ticketId = "TICKET_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                subject = subject.ifBlank { "General Inquiry" },
                chatHistoryJson = historyJson,
                status = "OPEN"
            )
            ticketDao.insertTicket(ticket)
            _ticketMessage.value = "Support Ticket #${ticket.ticketId} created! An Admin will review your chat history shortly."
        }
    }

    fun clearTicketMessage() {
        _ticketMessage.value = null
    }
}
