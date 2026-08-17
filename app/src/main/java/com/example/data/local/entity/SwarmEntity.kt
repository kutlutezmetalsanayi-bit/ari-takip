package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swarms")
data class SwarmEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val hiveId: String,
  val apiaryId: String,
  val eventType: String = "Oğul Eğilimi", // "Oğul Eğilimi", "Meme Dikti", "Oğul Verdi", "Oğul Yakalandı", "Bölme Yapıldı (Kaynak)", "Bölme Oluşturuldu (Yeni Kovan)"
  val eventDate: Long = System.currentTimeMillis(),
  val tendencyLevel: String = "Yok", // "Yok", "Düşük", "Yüksek"
  val queenCellsStatus: String = "Yok", // "Yok", "Açık Meme", "Kapalı Meme", "Memeler Bozuldu"
  val relatedHiveId: String? = null,
  val relatedHiveNumber: Int? = null,
  val actionTaken: String = "", // "Memeler temizlendi", "Kat atıldı", "Bölme yapıldı", "Yeni kovana aktarıldı", "Gözlemde"
  val notes: String = "",
  val photoUri: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
