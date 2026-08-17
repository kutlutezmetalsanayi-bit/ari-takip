package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
  @Query("SELECT * FROM photos ORDER BY date DESC")
  fun getAllPhotos(): Flow<List<PhotoEntity>>

  @Query("SELECT * FROM photos ORDER BY date DESC")
  suspend fun getAllPhotosSync(): List<PhotoEntity>

  @Query("SELECT * FROM photos WHERE hiveId = :hiveId ORDER BY date DESC")
  fun getPhotosForHive(hiveId: String): Flow<List<PhotoEntity>>

  @Query("SELECT * FROM photos WHERE targetId = :targetId ORDER BY date DESC")
  fun getPhotosForTarget(targetId: String): Flow<List<PhotoEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPhoto(photo: PhotoEntity)

  @Query("DELETE FROM photos WHERE id = :id")
  suspend fun deletePhotoById(id: String)

  @Query("DELETE FROM photos")
  suspend fun clearAllPhotos()
}
