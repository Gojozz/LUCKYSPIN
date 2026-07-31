package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.WalletTransactionEntity
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    user: UserEntity?,
    transactions: List<WalletTransactionEntity>,
    onDepositRequest: (Long) -> Unit,
    onWithdrawRequest: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDepositModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }

    var depositAmountText by remember { mutableStateOf("1000") }
    var withdrawAmountText by remember { mutableStateOf("500") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
            .padding(16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
            }
            Text(
                text = "WALLETS & COINS",
                color = GoldPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Wallet Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bonus Coin Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C00)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BonusCoinColor))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BONUS COINS", color = BonusCoinColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user?.bonusCoins ?: 0}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Playable Only • Non-withdrawable", color = TextSecondary, fontSize = 9.sp)
                }
            }

            // Real Coin Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF002B11)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RealCoinColor))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("REAL COINS", color = RealCoinColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user?.realCoins ?: 0}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Playable & Withdrawable", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showDepositModal = true },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RealCoinColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PurpleDarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("DEPOSIT", color = PurpleDarkBackground, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = { showWithdrawModal = true },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PurpleDarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("WITHDRAW", color = PurpleDarkBackground, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TRANSACTION HISTORY",
            color = GoldAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tx.type.replace("_", " "),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tx.note,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = if (tx.amount > 0) "+${tx.amount}" else "${tx.amount}",
                            color = if (tx.amount > 0) WinnerGreen else LoserRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    // Deposit Modal
    if (showDepositModal) {
        AlertDialog(
            onDismissRequest = { showDepositModal = false },
            title = { Text("Simulate Deposit", color = GoldPrimary) },
            text = {
                Column {
                    Text("Select coin deposit package:", color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it },
                        label = { Text("Coins Amount") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = depositAmountText.toLongOrNull() ?: 1000L
                    onDepositRequest(amount)
                    showDepositModal = false
                }) {
                    Text("CONFIRM DEPOSIT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositModal = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = PurpleSurface
        )
    }

    // Withdraw Modal
    if (showWithdrawModal) {
        AlertDialog(
            onDismissRequest = { showWithdrawModal = false },
            title = { Text("Withdraw Real Coins", color = GoldPrimary) },
            text = {
                Column {
                    Text("Enter Real Coins amount to withdraw:", color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        label = { Text("Amount") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = withdrawAmountText.toLongOrNull() ?: 500L
                    onWithdrawRequest(amount)
                    showWithdrawModal = false
                }) {
                    Text("SUBMIT WITHDRAWAL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawModal = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = PurpleSurface
        )
    }
}
