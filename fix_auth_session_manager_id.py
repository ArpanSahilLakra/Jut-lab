import re

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'r') as f:
    text = f.read()

# Replace getStudentProfile() with getStudentProfileById(user.id)
text = text.replace('labDao.getStudentProfile().firstOrNull()', 'labDao.getStudentProfileById(user.id).firstOrNull()')

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'w') as f:
    f.write(text)

