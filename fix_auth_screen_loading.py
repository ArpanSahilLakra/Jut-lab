import re

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'r') as f:
    text = f.read()

old_button = '''            onClick = {
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
                        onAuthSuccess()
                        // Loading state will be removed when screen is popped
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                        isLoading = false
                    }
                }
            }'''

new_button = '''            onClick = {
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
            }'''

text = text.replace(old_button, new_button)

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'w') as f:
    f.write(text)

