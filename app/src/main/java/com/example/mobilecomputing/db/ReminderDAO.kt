package com.example.mobilecomputing.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ReminderDAO {
    @Transaction
    @Insert
    fun insert(reminderInfo: ReminderInfo): Long

    @Query("DELETE FROM reminderInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM reminderInfo")
    fun getReminderInfos(): List<ReminderInfo>
}