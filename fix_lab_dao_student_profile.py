import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    text = f.read()

# Modify getStudentProfile
old_query = '@Query("SELECT * FROM student_profile LIMIT 1")\n  fun getStudentProfile(): Flow<StudentProfileEntity?>'
new_query = '@Query("SELECT * FROM student_profile LIMIT 1")\n  fun getStudentProfile(): Flow<StudentProfileEntity?>\n\n  @Query("SELECT * FROM student_profile WHERE id = :userId LIMIT 1")\n  fun getStudentProfileById(userId: String): Flow<StudentProfileEntity?>'

text = text.replace(old_query, new_query)

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(text)

