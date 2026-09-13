import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# Replace import if needed
if 'import com.example.repository.AuthSessionManager' not in text:
    text = text.replace('import com.example.repository.AuthRepository', 'import com.example.repository.AuthRepository\nimport com.example.repository.AuthSessionManager\nimport com.example.repository.AuthState\nimport androidx.lifecycle.lifecycleScope')

# Initialize in onCreate
if 'AuthSessionManager.initialize' not in text:
    text = text.replace('appPreferences = AppPreferences(applicationContext)', 'appPreferences = AppPreferences(applicationContext)\n        AuthSessionManager.initialize(labDao, lifecycleScope)')

# Update compose state
if 'val authState by AuthSessionManager.authState.collectAsState()' not in text:
    text = text.replace('var currentRoute by remember { mutableStateOf("startup") }', 'var currentRoute by remember { mutableStateOf("startup") }\n                    val authState by AuthSessionManager.authState.collectAsState()')

# change startup logic
old_startup = '"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository().getCurrentUserId() == null) "auth" else "home" })'
new_startup = '''"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { 
                                            when(val state = authState) {
                                                is AuthState.Authenticated -> {
                                                    userRole = state.role
                                                    currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                                }
                                                else -> currentRoute = "auth"
                                            }
                                        })'''

text = text.replace(old_startup, new_startup)

# change auth success logic
old_auth = '"auth" -> AuthScreen(onAuthSuccess = { currentRoute = "home" })'
new_auth = '''"auth" -> AuthScreen(onAuthSuccess = { 
                                            val state = AuthSessionManager.authState.value
                                            if (state is AuthState.Authenticated) {
                                                userRole = state.role
                                                currentRoute = if (state.role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                                            } else {
                                                currentRoute = "student_dashboard" // fallback
                                            }
                                        })'''
text = text.replace(old_auth, new_auth)


with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

