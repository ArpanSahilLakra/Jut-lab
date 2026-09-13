import re
import os

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        text = f.read()
    if old in text:
        text = text.replace(old, new)
        with open(filepath, 'w') as f:
            f.write(text)

# StudentDashboardScreen
replace_in_file('app/src/main/java/com/example/ui/screens/StudentDashboardScreen.kt', 
                'labDao.getStudentProfile().collectAsState(initial = null)', 
                'labDao.getStudentProfileById(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = null)')
replace_in_file('app/src/main/java/com/example/ui/screens/StudentDashboardScreen.kt', 
                'labDao.getAllProgress().collectAsState(initial = emptyList())', 
                'labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())')

# HomeScreen
replace_in_file('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 
                'labDao.getStudentProfile().collectAsState(initial = null)', 
                'labDao.getStudentProfileById(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = null)')
replace_in_file('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 
                'labDao.getAllProgress().collectAsState(initial = emptyList())', 
                'labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())')

# DynamicExperimentScreen
replace_in_file('app/src/main/java/com/example/ui/screens/DynamicExperimentScreen.kt', 
                'labDao.getAllProgress().firstOrNull()?.find', 
                'labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").firstOrNull()?.find')

# LabReportScreen
replace_in_file('app/src/main/java/com/example/ui/screens/LabReportScreen.kt', 
                'labDao.getAllLabReports().collectAsState(initial = emptyList())', 
                'labDao.getLabReportsForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())')

