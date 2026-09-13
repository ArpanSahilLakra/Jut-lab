import re

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'r') as f:
    text = f.read()

# Add clearAllData to LabDao
new_method = '''
  @Query("DELETE FROM student_profile")
  suspend fun clearProfiles()
  
  @Query("DELETE FROM experiment_progress")
  suspend fun clearProgress()
  
  @Query("DELETE FROM lab_reports")
  suspend fun clearReports()
  
  @Query("DELETE FROM bookmarks")
  suspend fun clearBookmarks()
  
  @Query("DELETE FROM sync_queue")
  suspend fun clearSyncQueue()
  
  @Query("DELETE FROM activities")
  suspend fun clearActivities()
  
  @Transaction
  suspend fun clearUserSpecificData() {
      clearProfiles()
      clearProgress()
      clearReports()
      clearBookmarks()
      clearSyncQueue()
      clearActivities()
  }
'''

if 'suspend fun clearUserSpecificData()' not in text:
    # insert before the closing brace of LabDao
    text = text.replace('}\n\n@Database', new_method + '\n}\n\n@Database')

with open('app/src/main/java/com/example/data/LabDatabase.kt', 'w') as f:
    f.write(text)

