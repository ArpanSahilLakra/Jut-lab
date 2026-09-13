import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# I'll just write a script to replace the bad block with the correct block

bad_block = """                                        "startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository()
                                        "quiz" -> QuizScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "calculator" -> ElectronicsCalculatorScreen(onBack = { currentRoute = "home" })
                                        "ic_pinout" -> IcPinoutScreen(onBack = { currentRoute = "home" })
                                        "bookmarks" -> BookmarksScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "engineering_converter" -> EngineeringConverterScreen(onBack = { currentRoute = "home" })
                                        "ai_tutor" -> AiTutorScreen(onBack = { currentRoute = "home" })
                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" }).getCurrentUserId() == null) "auth" else "home" })"""

good_block = """                                        "startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository().getCurrentUserId() == null) "auth" else "home" })
                                        "quiz" -> QuizScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "calculator" -> ElectronicsCalculatorScreen(onBack = { currentRoute = "home" })
                                        "ic_pinout" -> IcPinoutScreen(onBack = { currentRoute = "home" })
                                        "bookmarks" -> BookmarksScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "engineering_converter" -> EngineeringConverterScreen(onBack = { currentRoute = "home" })
                                        "ai_tutor" -> AiTutorScreen(onBack = { currentRoute = "home" })
                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })"""

text = text.replace(bad_block, good_block)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

