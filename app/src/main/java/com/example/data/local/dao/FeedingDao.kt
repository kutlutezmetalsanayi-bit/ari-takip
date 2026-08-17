package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FeedingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
  @Query("SELECT * FROM feedings ORDER BY feedingDate DESC")
  fun getAllFeedings(): Flow<List<FeedingEntity>>

  @Query("SELECT * FROM feedings ORDER BY feedingDate DESC")
  suspend fun getAllFeedingsSync(): List<FeedingEntity>

  @Query("SELECT * FROM feedings WHERE hiveId = :hiveId ORDER BY feedingDate DESC")
  fun getFeedingsForHive(hiveId: String): Flow<List<FeedingEntity>>

  @Query("SELECT * FROM feedings WHERE apiaryId = :apiaryId ORDER BY feedingDate DESC")
  fun getFeedingsForApiary(apiaryId: String): Flow<List<FeedingEntity>>

  @Query("SELECT * FROM feedings WHERE id = :id")
  suspend fun getFeedingById(id: String): FeedingEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeeding(feeding: FeedingEntity)

  @Update
  suspend fun updateFeeding(feeding: FeedingEntity)

  @Query("DELETE FROM feedings WHERE id = :id")
  suspend fun deleteFeedingById(id: String)

  @Query("DELETE FROM feedings")
  suspend fun clearAllFeedings()
}
