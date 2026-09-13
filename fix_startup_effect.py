import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# Remove var startupFinished
text = text.replace('var startupFinished by remember { mutableStateOf(false) }\n', '')

# Replace the LaunchedEffect
old_effect = """
                    LaunchedEffect(authState, startupFinished) {
                        if (startupFinished) {
                            when (val state = authState) {
                                is AuthState.Authenticated -> {
                                    userRole = state.role
                                    if (currentRoute == "auth" || currentRoute == "startup") {
                                        currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                    }
                                }
                                is AuthState.Unauthenticated, is AuthState.Error -> {
                                    if (currentRoute == "startup" || currentRoute != "auth") {
                                        currentRoute = "auth"
                                    }
                                }
                                else -> {} // Still checking
                            }
                        } else {
                           // If startup isn't finished, only handle Unauthenticated if we were somehow on a protected route (shouldn't happen)
                           if (authState is AuthState.Unauthenticated && currentRoute != "startup" && currentRoute != "auth") {
                               currentRoute = "auth"
                           }
                        }
                    }
"""

new_effect = """
                    LaunchedEffect(authState) {
                        when (val state = authState) {
                            is AuthState.Authenticated -> {
                                userRole = state.role
                                if (currentRoute == "auth" || currentRoute == "startup") {
                                    currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                }
                            }
                            is AuthState.Unauthenticated, is AuthState.Error -> {
                                if (currentRoute == "startup" || currentRoute != "auth") {
                                    currentRoute = "auth"
                                }
                            }
                            else -> {} // CheckingSession -> remain on startup
                        }
                    }
"""

text = text.replace(old_effect, new_effect)

# Update onStartupComplete to do nothing, let the state drive it
old_startup = '''"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { 
                                            startupFinished = true
                                        })'''

new_startup = '''"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { 
                                            // Animation finished. State is driven by AuthSessionManager.
                                            if (authState is AuthState.Unauthenticated || authState is AuthState.Error) {
                                                currentRoute = "auth"
                                            } else if (authState is AuthState.Authenticated) {
                                                currentRoute = if ((authState as AuthState.Authenticated).role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                            }
                                        })'''

text = text.replace(old_startup, new_startup)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

