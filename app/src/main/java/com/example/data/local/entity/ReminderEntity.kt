package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val apiaryId: String? = null,
  val hiveId: String? = null,
  val title: String,
  val date: Long,
  val isCompleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
