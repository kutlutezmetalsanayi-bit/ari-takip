package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apiaries")
data class ApiaryEntity(
  @PrimaryKey val id: String,
  val userId: String = "user_default",
  val name: String,
  val country: String = "Türkiye",
  val city: String = "",
  val district: String = "",
  val address: String = "",
  val latitude: Double = 37.1305, // Default Muğla (famous Turkish beekeeping center)
  val longitude: Double = 28.3228,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isActive: Boolean = true
) {
  val displayLocation: String
    get() {
      return when {
        city.isNotBlank() && district.isNotBlank() -> "$city / $district"
        city.isNotBlank() -> city
        district.isNotBlank() -> district
        address.isNotBlank() -> address
        else -> "Konum belirtilmedi"
      }
    }

  val displayCityDistrict: String
    get() {
      return when {
        district.isNotBlank() && city.isNotBlank() -> "$district, $city"
        district.isNotBlank() -> district
        city.isNotBlank() -> city
        address.isNotBlank() -> address
        else -> name
      }
    }
}
