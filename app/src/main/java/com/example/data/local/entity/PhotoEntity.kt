package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String? = null,
  val apiaryId: String? = null,
  val targetType: String = "HIVE", // "HIVE", "INSPECTION", "FEEDING", "GENERAL"
  val targetId: String = "",
  val localUri: String,
  val cloudUrl: String? = null,
  val date: Long = System.currentTimeMillis(),
  val notes: String = ""
)
