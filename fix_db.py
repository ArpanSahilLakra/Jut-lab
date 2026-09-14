import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    content = f.read()

# Just remove the extra `val academicDao = database.academicDao()` I added
content = content.replace('val academicDao = database.academicDao()\n        \n        // --- SEMESTER 1 ---', '// --- SEMESTER 1 ---')

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(content)
