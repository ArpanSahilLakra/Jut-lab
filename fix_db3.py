import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace('val academicDao = database.academicDao()', '')
content = re.sub(
    r'(suspend fun populateInitialData\(database: LabDatabase\) \{)',
    r'\1\n      val academicDao = database.academicDao()\n',
    content
)

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(content)
