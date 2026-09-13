with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'r') as f:
    code = f.read()

target = '        TextButton(onClick = { isLogin = !isLogin }) {'
google_button = '''        Spacer(modifier = Modifier.height(16.dp))
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
        TextButton(onClick = { isLogin = !isLogin }) {'''

code = code.replace(target, google_button)

with open('app/src/main/java/com/example/ui/screens/AuthScreen.kt', 'w') as f:
    f.write(code)
