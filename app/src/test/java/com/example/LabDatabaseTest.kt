package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.LabDao
import com.example.data.LabDatabase
import com.example.data.StudentProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LabDatabaseTest {
    private lateinit var labDao: LabDao
    private lateinit var db: LabDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, LabDatabase::class.java
        ).allowMainThreadQueries().build()
        labDao = db.labDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetProfile() = runBlocking {
        val profile = StudentProfileEntity(
            id = "user123",
            email = "student@jut.edu",
            role = "student",
            xp = 1500,
            level = 3,
            currentStreak = 5,
            longestStreak = 10,
            experimentsCompleted = 12,
            totalStudyTime = 3600L
        )

        labDao.upsertStudentProfile(profile)
        
        val loaded = labDao.getStudentProfile().first()
        assertEquals(loaded?.email, "student@jut.edu")
        assertEquals(loaded?.xp, 1500)
    }
}
