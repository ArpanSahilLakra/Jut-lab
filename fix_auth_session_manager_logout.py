import re

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'r') as f:
    text = f.read()

text = text.replace('// labDao.clearAllData() // If possible, but we don\'t have it.', 'labDao.clearUserSpecificData()')

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'w') as f:
    f.write(text)

