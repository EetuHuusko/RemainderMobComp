package com.example.mobilecomputing

import android.content.Context
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mobilecomputing.db.AppDatabase

class ReminderWorker(appContext:Context, workerParameters: WorkerParameters) :
    Worker(appContext,workerParameters) {

    override fun doWork(): Result {
        val text = inputData.getString("message")
        val uid = inputData.getInt("uid", 0)
        MainActivity.showNotification(applicationContext,text!!, uid)

        val db = Room
            .databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "com.example.mobilecomputing.db"
            )
            .build()

        db.reminderDAO().updateSeen(true, uid)
        return   Result.success()
    }
}