package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Queue item for offline changes waiting to be synced to cloud/Firebase.
 */
@Entity(tableName = "sync_queue")
data class SyncEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val entityType: String, // "APIARY", "HIVE", "INSPECTION", "FEEDING", "PHOTO", "REMINDER"
  val operation: String,  // "INSERT", "UPDATE", "DELETE"
  val recordId: String,
  val payloadJson: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
  val status: String = "PENDING", // "PENDING", "SYNCING", "FAILED", "SYNCED"
  val lastError: String? = null
)
