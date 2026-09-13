import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

bad_block = """                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" }).getCurrentUserId() == null) "auth" else "home" })"""

good_block = """                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })"""

text = text.replace(bad_block, good_block)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

