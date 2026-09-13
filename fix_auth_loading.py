import re

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'r') as f:
    text = f.read()

# Add isLoading
if 'var isLoading' not in text:
    text = text.replace('var errorMessage by remember { mutableStateOf<String?>(null) }', 'var errorMessage by remember { mutableStateOf<String?>(null) }\n    var isLoading by remember { mutableStateOf(false) }')

old_button = '''            onClick = {
                scope.launch {
                    errorMessage = null
                    val result = if (isLogin) {
                        authRepository.signIn(email, password)
                    } else {
                        authRepository.signUp(email, password)
                    }
                    if (result.isSuccess) {
                        onAuthSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
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
                        onAuthSuccess()
                        // Loading state will be removed when screen is popped
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                        isLoading = false
                    }
                }
            }'''

text = text.replace(old_button, new_button)

# Also update the button text to show "LOADING..." if isLoading is true
if 'text = if (isLoading) "PLEASE WAIT..." else if (isLogin) "LOGIN" else "REGISTER"' not in text:
    text = text.replace('text = if (isLogin) "LOGIN" else "REGISTER",', 'text = if (isLoading) "PLEASE WAIT..." else if (isLogin) "LOGIN" else "REGISTER",')

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'w') as f:
    f.write(text)

