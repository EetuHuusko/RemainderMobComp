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

    @Query("UPDATE reminderInfo SET reminder_seen = :hasSeen WHERE uid = :id")
    fun updateSeen(hasSeen: Boolean, id: Int)

    @Query("SELECT * FROM reminderInfo")
    fun getReminderInfos(): List<ReminderInfo>

    @Query("SELECT * FROM reminderInfo WHERE creation_id = :creator AND reminder_seen = 1")
    fun getSeenReminderInfos(creator: String): List<ReminderInfo>

    @Update
    fun updateReminderInfo(vararg reminderInfo: ReminderInfo)
}