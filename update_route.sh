sed -i 's/.*"submission_detail".*/                "submission_detail" -> SubmissionDetailScreen(reportId = selectedReportId, onBack = { currentRoute = "teacher_workspace" })/' app/src/main/java/com/example/MainActivity.kt
sed -i '/val reportId = "some_id"/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/}/d' app/src/main/java/com/example/MainActivity.kt
