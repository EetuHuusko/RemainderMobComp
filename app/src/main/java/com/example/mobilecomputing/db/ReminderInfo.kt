package com.example.mobilecomputing.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminderInfo")
data class ReminderInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "time") var time: String,
    @ColumnInfo(name = "location") var location: String
)
