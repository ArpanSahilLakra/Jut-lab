import re

with open('app/src/main/java/com/example/ui/screens/VivaEngineScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("SuccessGreen", "SafeGreen")
content = content.replace("WarningYellow", "AmberAccent")

with open('app/src/main/java/com/example/ui/screens/VivaEngineScreen.kt', 'w') as f:
    f.write(content)
