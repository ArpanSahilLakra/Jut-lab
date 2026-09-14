import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace('// --- SEMESTER 1 ---', 'val academicDao = database.academicDao()\n        // --- SEMESTER 1 ---')
content = content.replace('val academicDao = database.academicDao()\n      val academicDao = database.academicDao()', 'val academicDao = database.academicDao()')

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(content)
