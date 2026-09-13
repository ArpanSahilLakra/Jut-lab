import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    text = f.read()

# Update getAllProgress
text = text.replace('@Query("SELECT * FROM experiment_progress")\n  fun getAllProgress(): Flow<List<ExperimentProgressEntity>>', '@Query("SELECT * FROM experiment_progress")\n  fun getAllProgress(): Flow<List<ExperimentProgressEntity>>\n\n  @Query("SELECT * FROM experiment_progress WHERE userId = :userId")\n  fun getProgressForUser(userId: String): Flow<List<ExperimentProgressEntity>>')

# Update getAllLabReports
text = text.replace('@Query("SELECT * FROM lab_reports ORDER BY updatedAt DESC")\n  fun getAllLabReports(): Flow<List<LabReportEntity>>', '@Query("SELECT * FROM lab_reports ORDER BY updatedAt DESC")\n  fun getAllLabReports(): Flow<List<LabReportEntity>>\n\n  @Query("SELECT * FROM lab_reports WHERE userId = :userId ORDER BY updatedAt DESC")\n  fun getLabReportsForUser(userId: String): Flow<List<LabReportEntity>>')

# Update getAllBookmarks
text = text.replace('@Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")\n  fun getAllBookmarks(): Flow<List<BookmarkEntity>>', '@Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")\n  fun getAllBookmarks(): Flow<List<BookmarkEntity>>\n\n  @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")\n  fun getBookmarksForUser(userId: String): Flow<List<BookmarkEntity>>')

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(text)

