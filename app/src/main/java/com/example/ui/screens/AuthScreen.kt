package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isResetMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleDarkBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon Branding
        Image(
            painter = painterResource(id = R.drawable.img_app_icon_1785452995089),
            contentDescription = "Lucky Spin Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "LUCKY SPIN",
            color = GoldPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "MULTIPLAYER CASINO ARENA",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGold))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        isResetMode -> "RESET PASSWORD"
                        isRegisterMode -> "CREATE ACCOUNT (+1,000 BONUS)"
                        else -> "LOGIN TO PLAY"
                    },
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CardBorderGold,
                            focusedLabelColor = GoldPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CardBorderGold,
                        focusedLabelColor = GoldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CardBorderGold,
                        focusedLabelColor = GoldPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (authState != null) {
                    Text(
                        text = authState!!,
                        color = LoserRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        if (isResetMode) {
                            authViewModel.resetPassword(email, password)
                        } else if (isRegisterMode) {
                            authViewModel.register(email, password, name, onAuthSuccess)
                        } else {
                            authViewModel.login(email, password, onAuthSuccess)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PurpleDarkBackground)
                    } else {
                        Text(
                            text = when {
                                isResetMode -> "RESET PASSWORD"
                                isRegisterMode -> "REGISTER & GET 1,000 COINS"
                                else -> "LOGIN"
                            },
                            color = PurpleDarkBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        isRegisterMode = !isRegisterMode
                        isResetMode = false
                        authViewModel.clearError()
                    }) {
                        Text(
                            text = if (isRegisterMode) "Already have account? Login" else "New Player? Register",
                            color = CyanSecondary,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(onClick = {
                        isResetMode = !isResetMode
                        authViewModel.clearError()
                    }) {
                        Text(
                            text = if (isResetMode) "Back to Login" else "Forgot Password?",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Super Admin Quick Access Button
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    email = "jojo.mrj82@gmail.com"
                    password = "admin123"
                    authViewModel.login(email, password, onAuthSuccess)
                }) {
                    Text(
                        text = "⚡ Quick Super Admin Login (jojo.mrj82@gmail.com)",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
