package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.network.ApiClient
import com.geopulse.app.network.RegisterRequestDto
import com.geopulse.app.network.UserDto
import com.geopulse.app.network.UserSession
import com.geopulse.app.ui.theme.*
import com.geopulse.app.util.GeoLanguageManager
import com.geopulse.app.util.GeoStrings
import kotlinx.coroutines.launch

class CreateAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeoLanguageManager.init(this)
        setContent {
            GeoPulseTheme {
                var fullName by remember { mutableStateOf("") }
                var farmName by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isLoading by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                val isDark = ThemePreferences.isDarkTheme
                val bgBrush = if (isDark) {
                    Brush.radialGradient(
                        colors = listOf(EvergreenDark.copy(alpha = 0.45f), DarkBackground),
                        radius = 1000f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(LightSurfaceElevated, LightBackground),
                        radius = 1000f
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgBrush)
                            .navigationBarsPadding()
                            .statusBarsPadding()
                            .imePadding()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 480.dp),
                            contentPadding = PaddingValues(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(52.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Agriculture,
                                                contentDescription = "GeoPulse Logo",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = {
                                            val nextLang = if (GeoLanguageManager.isTamil) "en" else "ta"
                                            GeoLanguageManager.setLanguage(this@CreateAccountActivity, nextLang)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🌐", fontSize = 12.sp)
                                            Text(
                                                text = if (GeoLanguageManager.isTamil) "English" else "தமிழ்",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "GeoPulse",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = GeoStrings.appTagline,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 480.dp)
                                        .glassSurface(cornerRadius = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = GeoStrings.createAccount,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = GeoLanguageManager.t(
                                                "Register your agricultural operation",
                                                "உங்கள் பண்ணை மற்றும் நில விவரங்களை பதிவு செய்யுங்கள்"
                                            ),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        OutlinedTextField(
                                            value = fullName,
                                            onValueChange = { fullName = it; errorMessage = null },
                                            label = { Text(GeoStrings.fullNameLabel) },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = farmName,
                                            onValueChange = { farmName = it; errorMessage = null },
                                            label = { Text(GeoStrings.farmNameLabel) },
                                            leadingIcon = { Icon(Icons.Default.Agriculture, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it; errorMessage = null },
                                            label = { Text(GeoStrings.emailLabel) },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it; errorMessage = null },
                                            label = { Text(GeoStrings.passwordLabel) },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = confirmPassword,
                                            onValueChange = { confirmPassword = it; errorMessage = null },
                                            label = { Text(GeoLanguageManager.t("Confirm Password", "கடவுச்சொல்லை மீண்டும் உள்ளிடவும்")) },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        if (errorMessage != null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = errorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                        Button(
                                            onClick = {
                                                if (fullName.isBlank() || farmName.isBlank()) {
                                                    errorMessage = GeoLanguageManager.t("Please enter your name and farm name.", "உங்கள் பெயர் மற்றும் பண்ணை பெயரை உள்ளிடவும்.")
                                                } else if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
                                                    errorMessage = GeoStrings.invalidEmailError
                                                } else if (password.length < 6) {
                                                    errorMessage = GeoStrings.shortPasswordError
                                                } else if (password != confirmPassword) {
                                                    errorMessage = GeoLanguageManager.t("Passwords do not match.", "கடவுச்சொற்கள் பொருந்தவில்லை.")
                                                } else {
                                                    isLoading = true
                                                    errorMessage = null
                                                    coroutineScope.launch {
                                                        try {
                                                            val req = RegisterRequestDto(
                                                                email = email.trim(),
                                                                password = password.trim(),
                                                                fullName = fullName.trim(),
                                                                farmName = farmName.trim(),
                                                                preferredLanguage = GeoLanguageManager.currentLanguage
                                                            )
                                                            val resp = ApiClient.service.register(req)
                                                            if (resp.isSuccessful && resp.body() != null) {
                                                                val auth = resp.body()!!
                                                                val user = auth.user ?: UserDto(
                                                                    id = -1,
                                                                    email = email.trim(),
                                                                    fullName = fullName.trim(),
                                                                    phone = null,
                                                                    farmName = farmName.trim(),
                                                                    totalAreaHa = 0.0,
                                                                    preferredLanguage = GeoLanguageManager.currentLanguage,
                                                                    themePreference = "SYSTEM"
                                                                )
                                                                UserSession.saveSession(this@CreateAccountActivity, auth.accessToken, user)
                                                                startActivity(Intent(this@CreateAccountActivity, Screen7Activity::class.java))
                                                                finish()
                                                            } else {
                                                                errorMessage = GeoLanguageManager.t(
                                                                    "Registration failed. Please try a different email.",
                                                                    "பதிவு தோல்வியுற்றது. வேறு மின்னஞ்சலை முயற்சிக்கவும்."
                                                                )
                                                            }
                                                        } catch (e: Exception) {
                                                            errorMessage = GeoLanguageManager.t(
                                                                "Cannot connect to server. Check network connection.",
                                                                "சர்வர் தொடர்பு தோல்வி. இணைய இணைப்பை சரிபார்க்கவும்."
                                                            )
                                                        } finally {
                                                            isLoading = false
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !isLoading,
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = GeoStrings.createAccount,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        TextButton(onClick = {
                                            startActivity(Intent(this@CreateAccountActivity, LoginActivity::class.java))
                                            finish()
                                        }) {
                                            Text(
                                                text = GeoStrings.alreadyHaveAccount,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
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
}
