package com.example.mobilecomputing.db

import androidx.room.*

@Dao
interface ReminderDAO {
    @Transaction
    @Insert
    fun insert(reminderInfo: ReminderInfo): Long

    @Query("DELETE FROM reminderInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM reminderInfo WHERE uid = :id")
    fun getReminderInfo(id: Int): ReminderInfo

    @Query("SELECT * FROM reminderInfo")
    fun getReminderInfos(): List<ReminderInfo>

    @Update
    fun updateReminderInfo(vararg reminderInfo: ReminderInfo)
}