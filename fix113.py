import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if '"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository()' in line:
        lines[i] = '                                        "startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository().getCurrentUserId() == null) "auth" else "home" })\n'
        break

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(lines)

