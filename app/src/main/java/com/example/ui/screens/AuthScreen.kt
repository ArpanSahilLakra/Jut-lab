package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.AuthRepository
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val authRepository = remember { AuthRepository() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoHeader(
            title = if (isLogin) "LOGIN" else "SIGN UP",
            subtitle = "ECE Virtual Lab - Authenticate"
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        NeoButton(
            text = if (isLoading) "PLEASE WAIT..." else if (isLogin) "LOGIN" else "REGISTER",
            backgroundColor = TechBlue, textColor = Color.White,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (isLoading) return@NeoButton
                isLoading = true
                scope.launch {
                    errorMessage = null
                    val result = if (isLogin) {
                        authRepository.signIn(email, password)
                    } else {
                        authRepository.signUp(email, password)
                    }
                    if (result.isSuccess) {
                        if (!isLogin) {
                             errorMessage = "Success! Please check your email if confirmation is required, then login."
                             isLoading = false
                             isLogin = true
                        }
                        // For login, AuthSessionManager will handle state change to Authenticated
                        // If it fails to authenticate, it will stay loading. We should reset after a timeout or rely on the global state.
                        onAuthSuccess()
                        if (isLogin) {
                            kotlinx.coroutines.delay(2000)
                            isLoading = false
                        }
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                        isLoading = false
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.height(16.dp))
        NeoButton(
            text = "SIGN IN WITH GOOGLE",
            backgroundColor = Color.White, textColor = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (isLoading) return@NeoButton
                isLoading = true
                scope.launch {
                    errorMessage = null
                    val result = authRepository.signInWithGoogle()
                    if (result.isSuccess) {
                        onAuthSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign-in failed"
                        isLoading = false
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isLogin = !isLogin }) {
            Text(
                text = if (isLogin) "Need an account? Sign up" else "Already have an account? Login",
                color = TechBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
