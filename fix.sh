#!/bin/bash
sed -i 's/"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository()/"startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = if (AuthRepository().getCurrentUserId() == null) "auth" else "home" })/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/"learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" }).getCurrentUserId() == null) "auth" else "home" })/"learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })/g' app/src/main/java/com/example/MainActivity.kt
