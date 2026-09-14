import re

with open('app/src/main/java/com/example/ui/screens/MockExamScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('contentColor = Color.White', 'textColor = Color.White')

with open('app/src/main/java/com/example/ui/screens/MockExamScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/AcademicScreens.kt', 'r') as f:
    content2 = f.read()

bad_banner = """            }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToExam() },"""

good_banner = """            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToExam() },"""

content2 = content2.replace(bad_banner, good_banner)
content2 = content2.replace('Spacer(modifier = Modifier.height(8.dp))\n\n            \n            if (subjects.isEmpty()) {', 'Spacer(modifier = Modifier.height(8.dp))\n            }\n            \n            if (subjects.isEmpty()) {')

with open('app/src/main/java/com/example/ui/screens/AcademicScreens.kt', 'w') as f:
    f.write(content2)

