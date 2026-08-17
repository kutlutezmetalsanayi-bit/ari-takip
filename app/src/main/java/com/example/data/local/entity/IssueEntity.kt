package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class IssueEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String,
  val apiaryId: String,
  val category: String = "Varroa", // Varroa, Yavru Çürüklüğü, Kireç Hastalığı, Nozema, Mum Güvesi, Yağmalama, Zayıf Koloni / Sönme Riski, Ana Arı Kaybı / Sorunu, Zehirlenme / Zirai İlaç, Diğer
  val severity: String = "Orta", // Hafif, Orta, Ağır / Acil
  val status: String = "Açık", // Açık, Takipte, Çözüldü
  val detectedDate: Long = System.currentTimeMillis(),
  val resolvedDate: Long? = null,
  val treatmentNotes: String = "", // Uygulanan tedavi veya müdahale
  val notes: String = "",
  val photoUris: String = "", // Comma separated image paths
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
