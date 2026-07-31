package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminPanelScreen(
    currentUser: UserEntity?,
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Users, 1: Settings, 2: Jackpot, 3: Ads, 4: Audit Logs

    val users by adminViewModel.allUsers.collectAsState(initial = emptyList())
    val ads by adminViewModel.allAds.collectAsState(initial = emptyList())
    val settings by adminViewModel.gameSettings.collectAsState(initial = null)
    val jackpot by adminViewModel.globalJackpot.collectAsState(initial = null)
    val auditLogs by adminViewModel.auditLogs.collectAsState(initial = emptyList())
    val adminMessage by adminViewModel.adminMessage.collectAsState()

    var showCoinModal by remember { mutableStateOf(false) }
    var selectedUserForCoin by remember { mutableStateOf<UserEntity?>(null) }
    var bonusCoinDeltaText by remember { mutableStateOf("1000") }
    var realCoinDeltaText by remember { mutableStateOf("500") }

    var timerText by remember { mutableStateOf("120") }
    var feeText by remember { mutableStateOf("20") }
    var bonusText by remember { mutableStateOf("1000") }

    var adTitleText by remember { mutableStateOf("") }
    var adContentText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurpleSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
            }
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("SUPER ADMIN PANEL", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text("Permanent Super Admin: jojo.mrj82@gmail.com", color = TextSecondary, fontSize = 10.sp)
            }
        }

        if (adminMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = adminMessage!!,
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { adminViewModel.clearMessage() }) {
                        Text("OK", color = GoldPrimary)
                    }
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = PurpleSurfaceVariant,
            contentColor = GoldPrimary
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Users") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Settings") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Jackpot") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Ads") })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Audit Logs") })
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Users Management
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(users) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(u.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${u.email} • ${u.role}", color = GoldAccent, fontSize = 11.sp)
                                        Text("Bonus: ${u.bonusCoins} • Real: ${u.realCoins}", color = TextSecondary, fontSize = 10.sp)
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            selectedUserForCoin = u
                                            showCoinModal = true
                                        }) {
                                            Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = GoldPrimary)
                                        }

                                        if (u.email != "jojo.mrj82@gmail.com") {
                                            IconButton(onClick = {
                                                val newRole = if (u.role == "ADMIN") "USER" else "ADMIN"
                                                adminViewModel.setUserRole(currentUser?.email ?: "admin", u.id, newRole)
                                            }) {
                                                Icon(
                                                    imageVector = if (u.role == "ADMIN") Icons.Default.PersonOff else Icons.Default.VerifiedUser,
                                                    contentDescription = "Role",
                                                    tint = CyanSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Game Settings
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("GAME CONFIGURATION", color = GoldPrimary, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = timerText,
                            onValueChange = { timerText = it },
                            label = { Text("Default Round Duration (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        OutlinedTextField(
                            value = feeText,
                            onValueChange = { feeText = it },
                            label = { Text("System Fee Percentage (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        OutlinedTextField(
                            value = bonusText,
                            onValueChange = { bonusText = it },
                            label = { Text("New User Bonus Coins") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        Button(
                            onClick = {
                                val t = timerText.toIntOrNull() ?: 120
                                val f = feeText.toIntOrNull() ?: 20
                                val b = bonusText.toLongOrNull() ?: 1000L
                                adminViewModel.updateGameSettings(currentUser?.email ?: "admin", t, f, b)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("SAVE SETTINGS", color = PurpleDarkBackground, fontWeight = FontWeight.Black)
                        }
                    }
                }

                2 -> {
                    // Jackpot Management
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("GLOBAL JACKPOT CONTROL", color = GoldPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Current Pool: ${jackpot?.totalGlobalJackpotCoins ?: 0} COINS",
                            color = WinnerGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Button(
                            onClick = {
                                adminViewModel.triggerJackpotPayout(
                                    currentUser?.email ?: "admin",
                                    "LuckyWinner_${(100..999).random()}",
                                    10000L
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("TRIGGER 10,000 COIN PAYOUT", color = PurpleDarkBackground, fontWeight = FontWeight.Black)
                        }
                    }
                }

                3 -> {
                    // Ads Management
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("CREATE ANNOUNCEMENT / BANNER", color = GoldPrimary, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = adTitleText,
                            onValueChange = { adTitleText = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        OutlinedTextField(
                            value = adContentText,
                            onValueChange = { adContentText = it },
                            label = { Text("Content") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                        )

                        Button(
                            onClick = {
                                adminViewModel.saveAd(currentUser?.email ?: "admin", adTitleText, adContentText, "BANNER")
                                adTitleText = ""
                                adContentText = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("PUBLISH AD", color = PurpleDarkBackground, fontWeight = FontWeight.Black)
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ads) { ad ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(ad.title, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                            Text(ad.content, color = TextSecondary, fontSize = 11.sp)
                                        }

                                        IconButton(onClick = {
                                            adminViewModel.deleteAd(currentUser?.email ?: "admin", ad.id)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LoserRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Audit Logs
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(log.action, color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(log.details, color = TextPrimary, fontSize = 11.sp)
                                    Text("By: ${log.performedBy}", color = TextSecondary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Adjust Coin Dialog
    if (showCoinModal && selectedUserForCoin != null) {
        AlertDialog(
            onDismissRequest = { showCoinModal = false },
            title = { Text("Adjust Coins: ${selectedUserForCoin!!.name}", color = GoldPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = bonusCoinDeltaText,
                        onValueChange = { bonusCoinDeltaText = it },
                        label = { Text("Bonus Coins Delta (+/-)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = realCoinDeltaText,
                        onValueChange = { realCoinDeltaText = it },
                        label = { Text("Real Coins Delta (+/-)") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val b = bonusCoinDeltaText.toLongOrNull() ?: 0L
                    val r = realCoinDeltaText.toLongOrNull() ?: 0L
                    adminViewModel.adjustUserCoins(currentUser?.email ?: "admin", selectedUserForCoin!!.id, b, r)
                    showCoinModal = false
                }) {
                    Text("APPLY COIN ADJUSTMENT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoinModal = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = PurpleSurface
        )
    }
}
