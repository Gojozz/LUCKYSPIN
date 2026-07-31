package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AdvertisementEntity
import com.example.data.model.GlobalJackpotEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AdBannerView
import com.example.ui.components.CoinChipHeader
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: UserEntity?,
    globalJackpot: GlobalJackpotEntity?,
    ads: List<AdvertisementEntity>,
    onJoinRoomClick: () -> Unit,
    onWalletClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1785453008131),
                        contentDescription = "Casino Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x990F0814))
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = "LUCKY SPIN ARENA",
                                color = GoldPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Pick 1 of 25 numbers • Spin the wheel • Win the Pot!",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Global Jackpot Ticker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1500)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Jackpot",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GLOBAL JACKPOT POOL",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${globalJackpot?.totalGlobalJackpotCoins ?: 100000} COINS",
                            color = WinnerGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = onJoinRoomClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text(
                            text = "PLAY NOW",
                            color = PurpleDarkBackground,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Quick Join Arena Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MULTIPLAYER GAME ROOMS",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Room 1 Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleSurfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Room",
                                tint = CyanSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Golden Wheel Arena",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "1-5 Players • 120s Round • Min Bet 10 Coins",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = onJoinRoomClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                        ) {
                            Text("JOIN", color = PurpleDarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Advertisement Carousel Banner
            AdBannerView(ads = ads)

            // User Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR GAME STATISTICS",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Wins", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${user?.winsCount ?: 0}",
                                color = WinnerGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Losses", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${user?.lossesCount ?: 0}",
                                color = LoserRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Won", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${user?.totalWonCoins ?: 0}",
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Support Floating Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onSupportClick,
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanSecondary))
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "AI Support",
                        tint = CyanSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("24/7 SpinBot AI Customer Support", color = CyanSecondary)
                }
            }
        }
    }
}
