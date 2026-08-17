package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queens")
data class QueenEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String,
  val apiaryId: String,
  val status: String = "Var", // "Var", "Yok", "Yalancı Ana", "Bilinmiyor", "Eski / Değiştirildi"
  val year: Int = 2025,
  val breed: String = "Kafkas", // Kafkas, Karniyol, Belfast (Buckfast), Anadolu, Muğla Yerli, İtalyan, Diğer
  val markingColor: String = "Mavi", // Beyaz (1,6), Sarı (2,7), Kırmızı (3,8), Yeşil (4,9), Mavi (5,0), İşaretsiz
  val source: String = "Kendi Üretimimiz", // Satın Alındı, Kendi Üretimimiz, Doğal Oğuldan, Bölmeden, Bilinmiyor
  val installedDate: Long = System.currentTimeMillis(),
  val notes: String = "",
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)
