package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
  @PrimaryKey val id: String = "local_student",
  val name: String = "Student",
  val email: String = "",
  val role: String = "student",
  val xp: Int = 0,
  val level: Int = 1,
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val experimentsCompleted: Int = 0,
  val quizAttempts: Int = 0,
  val vivaAttempts: Int = 0,
  val totalStudyTime: Long = 0L,
  val lastActivityAt: Long = System.currentTimeMillis(),
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lab_reports")
data class LabReportEntity(
  @PrimaryKey val id: String,
  val userId: String = "",
  val experimentId: String,
  val title: String,
  val aim: String,
  val apparatus: String,
  val theory: String,
  val circuitDescription: String,
  val observations: String,
  val calculations: String,
  val result: String,
  val conclusion: String,
  val vivaScore: Int,
  val status: String = "DRAFT",
  val grade: String? = null,
  val feedback: String? = null,
  val reviewedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey val id: String,
  val userId: String = "",
  val type: String,
  val referenceId: String,
  val title: String,
  val description: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastAttemptAt: Long = 0,
    val status: String = "PENDING",
    val errorMessage: String? = null
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val experimentId: String,
    val description: String,
    val deadline: Long,
    val teacherId: String,
    val status: String = "UPCOMING",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val activityType: String,
    val referenceId: String,
    val metadata: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "teacher_student_summaries")
data class StudentSummaryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val rollNumber: String?,
    val xp: Int,
    val level: Int,
    val lastActivityAt: Long,
    val pendingSubmissions: Int,
    val completedExperiments: Int
)

@Entity(tableName = "teacher_submissions")
data class SubmissionEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val experimentId: String,
    val experimentTitle: String,
    val submittedAt: Long,
    val status: String // "SUBMITTED", "REVIEWED"
)
