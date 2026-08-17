package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
  @Query("SELECT * FROM reminders ORDER BY date ASC")
  fun getAllReminders(): Flow<List<ReminderEntity>>

  @Query("SELECT * FROM reminders ORDER BY date ASC")
  suspend fun getAllRemindersSync(): List<ReminderEntity>

  @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY date ASC")
  fun getPendingReminders(): Flow<List<ReminderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReminder(reminder: ReminderEntity)

  @Update
  suspend fun updateReminder(reminder: ReminderEntity)

  @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
  suspend fun setReminderCompleted(id: String, completed: Boolean)

  @Query("DELETE FROM reminders WHERE id = :id")
  suspend fun deleteReminderById(id: String)

  @Query("DELETE FROM reminders")
  suspend fun clearAllReminders()
}
