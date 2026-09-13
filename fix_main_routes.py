import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

insertion = """
                                        "quiz" -> QuizScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "calculator" -> ElectronicsCalculatorScreen(onBack = { currentRoute = "home" })
                                        "ic_pinout" -> IcPinoutScreen(onBack = { currentRoute = "home" })
                                        "bookmarks" -> BookmarksScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "engineering_converter" -> EngineeringConverterScreen(onBack = { currentRoute = "home" })
                                        "ai_tutor" -> AiTutorScreen(onBack = { currentRoute = "home" })
                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })
"""

# Find the block where we have "startup" -> StartupScreen
pattern = r'("startup"\s*->\s*StartupScreen[^)]*\))'
text = re.sub(pattern, r'\1' + insertion, text)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

