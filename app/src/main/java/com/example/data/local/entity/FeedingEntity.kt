package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedings")
data class FeedingEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String,
  val apiaryId: String,
  val feedingType: String = "Şurup (1:1)", // Şurup (1:1), Şurup (2:1), Kek, Protein Keki, Polen, Su, Diğer
  val amount: Double = 1.0,
  val unit: String = "Litre",             // ml, Litre, Gram, kg
  val feedingDate: Long = System.currentTimeMillis(),
  val notes: String = "",
  val photoUri: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
