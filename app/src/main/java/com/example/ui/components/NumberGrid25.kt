package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetEntity
import com.example.ui.theme.*

@Composable
fun NumberGrid25(
    selectedNumber: Int?,
    winningNumber: Int?,
    bets: List<BetEntity>,
    onNumberSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CHOOSE YOUR LUCKY NUMBER (1 - 25)",
                color = GoldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(260.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items((1..25).toList()) { num ->
                    val isSelected = selectedNumber == num
                    val isWinning = winningNumber == num
                    val betsOnNum = bets.filter { it.chosenNumber == num }

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isWinning -> WinnerGreen
                            isSelected -> GoldPrimary
                            betsOnNum.isNotEmpty() -> PurpleSurfaceVariant
                            else -> Color(0xFF221133)
                        },
                        label = "grid_bg"
                    )

                    val textColor = when {
                        isWinning || isSelected -> PurpleDarkBackground
                        else -> TextPrimary
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected || isWinning) 2.dp else 1.dp,
                                color = if (isWinning) Color.White else if (isSelected) GoldAccent else Color(0x33FFD700),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onNumberSelected(num) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$num",
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Bet chips indicator
                            if (betsOnNum.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val totalCoinsOnNum = betsOnNum.sumOf { it.betAmount }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) PurpleDarkBackground else BonusCoinColor)
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$totalCoinsOnNum",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GoldPrimary else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
