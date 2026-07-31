package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.CoinChipHeader
import com.example.ui.components.LuckySpinWheel
import com.example.ui.components.NumberGrid25
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameRoomScreen(
    user: UserEntity?,
    gameViewModel: GameViewModel,
    onBackClick: () -> Unit,
    onWalletClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRoom by gameViewModel.currentRoom.collectAsState()
    val bets by gameViewModel.bets.collectAsState()
    val timerSeconds by gameViewModel.timerSeconds.collectAsState()
    val selectedNumber by gameViewModel.selectedNumber.collectAsState()
    val betAmount by gameViewModel.betAmount.collectAsState()
    val walletType by gameViewModel.walletType.collectAsState()
    val isBetLocked by gameViewModel.isBetLocked.collectAsState()
    val isSpinning by gameViewModel.isSpinning.collectAsState()
    val winningNumber by gameViewModel.winningNumber.collectAsState()
    val lastOutcome by gameViewModel.lastOutcome.collectAsState()
    val message by gameViewModel.message.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(user) {
        user?.let {
            gameViewModel.enterRoom(it.id, it.name, it.avatarUrl)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
    ) {
        // Top Coin Header
        CoinChipHeader(
            user = user,
            onWalletClick = onWalletClick,
            onProfileClick = onProfileClick,
            onAdminClick = onAdminClick
        )

        // Room Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurpleSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                }
                Column {
                    Text(
                        text = currentRoom?.roomName ?: "Golden Wheel Arena",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Round #${currentRoom?.currentRoundNumber ?: 1} • Pot: ${bets.sumOf { it.betAmount }} Coins",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Countdown Timer Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (timerSeconds <= 10) LoserRed else GoldPrimary)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = PurpleDarkBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timerSeconds}s",
                        color = PurpleDarkBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Interactive Spinning Wheel
            LuckySpinWheel(
                targetWinningNumber = winningNumber,
                isSpinning = isSpinning,
                onSpinFinished = { },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Status / Outcome Banner
            AnimatedVisibility(visible = message != null || lastOutcome != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (winningNumber != null) {
                            Text(
                                text = "WINNING NUMBER: #$winningNumber",
                                color = WinnerGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (message != null) {
                            Text(
                                text = message!!,
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Players in Room (Human + AI System Bots)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "PLAYERS IN ROOM (${bets.size} bets locked)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (b in bets.take(5)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PurpleSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (b.isBot) "🤖 " else "👤 ",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${b.userName} (#${b.chosenNumber})",
                                        color = if (b.userId == user?.id) GoldPrimary else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 25 Number Selection Grid
            NumberGrid25(
                selectedNumber = selectedNumber,
                winningNumber = winningNumber,
                bets = bets,
                onNumberSelected = { gameViewModel.selectNumber(it) }
            )

            // Betting Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wallet Selector (Bonus Coins vs Real Coins)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wallet Type:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = walletType == "BONUS",
                                onClick = { gameViewModel.setWalletType("BONUS") },
                                label = { Text("Bonus Coins (${user?.bonusCoins ?: 0})", fontSize = 11.sp) },
                                enabled = !isBetLocked
                            )
                            FilterChip(
                                selected = walletType == "REAL",
                                onClick = { gameViewModel.setWalletType("REAL") },
                                label = { Text("Real Coins (${user?.realCoins ?: 0})", fontSize = 11.sp) },
                                enabled = !isBetLocked
                            )
                        }
                    }

                    // Bet Amount Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bet Amount:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(10L, 25L, 50L, 100L, 250L, 500L).forEach { amount ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (betAmount == amount) GoldPrimary else PurpleSurfaceVariant)
                                        .clickable(!isBetLocked) { gameViewModel.setBetAmount(amount) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$amount",
                                        color = if (betAmount == amount) PurpleDarkBackground else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Confirm Bet Button & Fast Preview Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { user?.let { gameViewModel.placeBet(it) } },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = !isBetLocked && selectedNumber != null,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PurpleDarkBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBetLocked) "BET LOCKED (#$selectedNumber)" else "CONFIRM BET ($betAmount COINS)",
                                color = PurpleDarkBackground,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        // Fast Spin Preview (15s)
                        IconButton(
                            onClick = { gameViewModel.setFastTimerPreview(15) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(PurpleSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Fast Spin", tint = CyanSecondary)
                        }
                    }
                }
            }
        }
    }
}
