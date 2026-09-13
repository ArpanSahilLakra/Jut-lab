import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# I want to replace the specific garbage line with the clean line.
# Garbage line:
# "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" }).getCurrentUserId() == null) "auth" else "home" })
pattern = r'"learning_material" -> LearningMaterialScreen\(onBack = \{ currentRoute = "home" \}\)\.getCurrentUserId\(\) == null\) "auth" else "home" \}\)'
replacement = r'"learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })'

text = re.sub(pattern, replacement, text)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

