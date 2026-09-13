import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

old_auth = '''"auth" -> AuthScreen(onAuthSuccess = { 
                                            val state = AuthSessionManager.authState.value
                                            if (state is AuthState.Authenticated) {
                                                userRole = state.role
                                                currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                            } else {
                                                currentRoute = "student_dashboard" // fallback
                                            }
                                        })'''

# Just leave it on auth, the LaunchedEffect will route it when AuthSessionManager completes
new_auth = '''"auth" -> AuthScreen(onAuthSuccess = { 
                                            // Do nothing immediately. 
                                            // AuthSessionManager is fetching the profile in the background.
                                            // When it emits AuthState.Authenticated, LaunchedEffect will route us.
                                        })'''

text = text.replace(old_auth, new_auth)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

