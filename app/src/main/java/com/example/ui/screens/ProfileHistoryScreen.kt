package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*

@Composable
fun ProfileHistoryScreen(
    user: UserEntity?,
    userBets: List<BetEntity>,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                }
                Text("USER PROFILE & HISTORY", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            IconButton(onClick = onLogoutClick) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = LoserRed)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PurpleDarkBackground, modifier = Modifier.size(52.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(user?.name ?: "Player", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "", color = TextSecondary, fontSize = 12.sp)

                if (user?.role == "SUPER_ADMIN" || user?.role == "ADMIN") {
                    Text(
                        text = "ROLE: ${user.role}",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bonus Coins", color = BonusCoinColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${user?.bonusCoins ?: 0}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Real Coins", color = RealCoinColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${user?.realCoins ?: 0}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Won", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${user?.totalWonCoins ?: 0}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("PAST BETS & ROUND HISTORY", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(userBets) { bet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Round #${bet.roundNumber} • Selected #${bet.chosenNumber}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Bet: ${bet.betAmount} ${bet.walletType} Coins", color = TextSecondary, fontSize = 11.sp)
                        }

                        Text(
                            text = if (bet.isWinner) "WINNER (+${bet.payoutAmount})" else "LOST",
                            color = if (bet.isWinner) WinnerGreen else LoserRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
