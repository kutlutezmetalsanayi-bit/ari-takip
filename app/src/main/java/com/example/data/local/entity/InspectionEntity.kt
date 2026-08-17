package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String,
  val apiaryId: String,
  val inspectionDate: Long = System.currentTimeMillis(),
  val queenSeen: Boolean = false,
  val broodEgg: Boolean = false,
  val broodLarva: Boolean = false,
  val broodCapped: Boolean = false,
  val colonyStrength: String = "Güçlü", // Zayıf, Orta, Güçlü, Çok Güçlü
  val honeyStatus: String = "Orta",     // Az, Orta, İyi, Çok İyi
  val pollenStatus: String = "Yeterli", // Az, Yeterli, Çok
  val behavior: String = "Sakin",       // Sakin, Normal, Hırçın
  val frameChanges: String = "",        // Kat eklendi, Kat çıkarıldı, Çerçeve eklendi, Çerçeve çıkarıldı
  val problems: String = "",            // Varroa belirtisi, Hastalık şüphesi, Ana arı problemi, Oğul eğilimi, Diğer
  val notes: String = "",
  val weatherSummary: String = "",      // e.g. "28°C, Açık, %45 Nem"
  val photoUris: String = "",           // Comma-separated list of photo URIs
  val createdAt: Long = System.currentTimeMillis()
)
