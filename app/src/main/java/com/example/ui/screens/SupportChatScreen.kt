package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SupportViewModel

@Composable
fun SupportChatScreen(
    user: UserEntity?,
    supportViewModel: SupportViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by supportViewModel.messages.collectAsState()
    val isAiThinking by supportViewModel.isAiThinking.collectAsState()
    val ticketMessage by supportViewModel.ticketMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showTicketModal by remember { mutableStateOf(false) }
    var ticketSubject by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurpleSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                }
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = CyanSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("SpinBot AI Support", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("24/7 AI Customer Assistant", color = TextSecondary, fontSize = 10.sp)
                }
            }

            TextButton(onClick = { showTicketModal = true }) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = CyanSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ticket", color = CyanSecondary, fontSize = 12.sp)
            }
        }

        // Ticket Success Notification Banner
        if (ticketMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = WinnerGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticketMessage!!,
                        color = WinnerGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { supportViewModel.clearTicketMessage() }) {
                        Text("OK", color = GoldPrimary)
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                )
                            )
                            .background(if (isUser) GoldPrimary else PurpleSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) PurpleDarkBackground else TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (isAiThinking) {
                item {
                    Text("SpinBot is typing...", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurpleSurface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask SpinBot about rules, coins, jackpots...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardBorderGold
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        supportViewModel.sendMessage(inputText, user)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(GoldPrimary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = PurpleDarkBackground)
            }
        }
    }

    // Ticket Creation Modal
    if (showTicketModal) {
        AlertDialog(
            onDismissRequest = { showTicketModal = false },
            title = { Text("Create Support Ticket", color = GoldPrimary) },
            text = {
                Column {
                    Text("Submit chat history to Admin for human agent review:", color = TextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ticketSubject,
                        onValueChange = { ticketSubject = it },
                        label = { Text("Issue Subject") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    supportViewModel.createSupportTicket(ticketSubject, user)
                    showTicketModal = false
                }) {
                    Text("CREATE TICKET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTicketModal = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = PurpleSurface
        )
    }
}
