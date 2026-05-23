package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ChineseApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { StudyRepository(database.studyDao()) }
}
