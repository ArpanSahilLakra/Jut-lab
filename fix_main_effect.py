import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# insert a LaunchedEffect for authState changes
effect_code = """
                    LaunchedEffect(authState) {
                        if (authState is AuthState.Authenticated) {
                            val role = (authState as AuthState.Authenticated).role
                            userRole = role
                            if (currentRoute == "auth" || currentRoute == "startup") {
                                currentRoute = if (role.lowercase() == "teacher") "teacher_workspace" else "student_dashboard"
                            }
                        } else if (authState is AuthState.Unauthenticated) {
                            if (currentRoute != "startup") {
                                currentRoute = "auth"
                            }
                        }
                    }
"""

if 'LaunchedEffect(authState)' not in text:
    text = text.replace('LaunchedEffect(currentRoute) {', effect_code + '                    LaunchedEffect(currentRoute) {')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

