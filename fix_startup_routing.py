import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

old_startup = '''"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { 
                                            when(val state = authState) {
                                                is AuthState.Authenticated -> {
                                                    userRole = state.role
                                                    currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                                }
                                                else -> currentRoute = "auth"
                                            }
                                        })'''

new_startup = '''"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { 
                                            // Handled by LaunchedEffect(authState) mostly, but just in case:
                                            if (authState is AuthState.Unauthenticated || authState is AuthState.Error) {
                                                currentRoute = "auth"
                                            }
                                        })'''

text = text.replace(old_startup, new_startup)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

