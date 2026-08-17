package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hives")
data class HiveEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val apiaryId: String,
  val hiveNumber: Int, // System-assigned sequential number per apiary, strictly read-only
  val photoUri: String? = null,
  val hiveType: String = "Langstroth", // Langstroth, Dadant, Karakovan, Top Bar, Diğer
  val queenYear: Int = 2025,
  val queenBreed: String = "Kafkas", // Kafkas, Karniyol, İtalyan, Belfast (Buckfast), Anadolu, Muğla Yerli, Belli Değil
  val colonyStrength: String = "Güçlü", // Zayıf, Orta, Güçlü, Çok Güçlü
  val notes: String = "",
  val status: String = "active", // "active", "archived"
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
