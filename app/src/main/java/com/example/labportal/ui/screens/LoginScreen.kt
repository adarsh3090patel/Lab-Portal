package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.ui.components.BackgroundCurves
import com.example.labportal.ui.components.PortalHeader
import com.example.labportal.ui.theme.LabPortalTheme
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.labportal.network.ApiConstants

@Composable
fun LoginScreen(
    onLoginClick: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onGoogleSignInClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var serverUrlInput by remember { mutableStateOf(ApiConstants.BASE_URL) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Server Configuration Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Backend Server URL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MmyrvTextDark
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Backend Server URL (Vercel Cloud is default):\n• Cloud: https://lab-portal-one.vercel.app/\n• Local IP: http://10.87.254.19:8000/",
                        fontSize = 12.sp,
                        color = MmyrvTextMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { serverUrlInput = ApiConstants.VERCEL_URL },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cloud),
                                contentDescription = null,
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Vercel", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { serverUrlInput = "http://10.87.254.19:8000/" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hotspot IP", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ApiConstants.setServerUrl(context, serverUrlInput)
                        showServerDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel", color = MmyrvTextMuted)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Subtle background curves in top-left and bottom-right corners
        BackgroundCurves()

        // Top-right Settings Button for Server IP
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = {
                    serverUrlInput = ApiConstants.BASE_URL
                    showServerDialog = true
                },
                modifier = Modifier
                    .background(color = Color(0xFFF1F5F9), shape = CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Server Settings",
                    tint = MmyrvTextDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // University Logo & Header Title
            PortalHeader()

            Spacer(modifier = Modifier.height(28.dp))

            // Main White Elevation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Card Header with Icon Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lavender Icon Container with School/Cap icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    color = MmyrvBadgeBg,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_school),
                                contentDescription = "Graduation Cap",
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Welcome Back",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Login to access your lab portal",
                                fontSize = 13.sp,
                                color = MmyrvTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Optional Error Message display
                    if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "University Email",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Email Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Password",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Lock Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                    ),
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginClick(email, password, rememberMe)
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MmyrvPrimary,
                                    uncheckedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remember me",
                                fontSize = 13.sp,
                                color = MmyrvTextMuted
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MmyrvPrimary,
                            modifier = Modifier.clickable { onForgotPasswordClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button with Arrow Icon
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLoginClick(email, password, rememberMe)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_login),
                                    contentDescription = "Login Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Login",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider with "or"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MmyrvBorder
                        )
                        Text(
                            text = "or",
                            fontSize = 13.sp,
                            color = MmyrvTextMuted,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MmyrvBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Continue with Google Button
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MmyrvBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MmyrvTextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Don't have an account? Register Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            fontSize = 13.sp,
                            color = MmyrvTextMuted
                        )
                        Text(
                            text = "Register",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MmyrvPrimary,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun LoginScreenPreview() {
    LabPortalTheme {
        LoginScreen()
    }
}

