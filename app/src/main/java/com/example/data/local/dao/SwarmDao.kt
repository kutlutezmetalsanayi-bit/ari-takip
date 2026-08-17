package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SwarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SwarmDao {
  @Query("SELECT * FROM swarms WHERE hiveId = :hiveId ORDER BY eventDate DESC")
  fun getSwarmEventsForHive(hiveId: String): Flow<List<SwarmEntity>>

  @Query("SELECT * FROM swarms WHERE relatedHiveId = :hiveId ORDER BY eventDate DESC")
  fun getRelatedSwarmEventsForHive(hiveId: String): Flow<List<SwarmEntity>>

  @Query("SELECT * FROM swarms ORDER BY eventDate DESC")
  fun getAllSwarmEvents(): Flow<List<SwarmEntity>>

  @Query("SELECT * FROM swarms ORDER BY eventDate DESC")
  suspend fun getAllSwarmEventsSync(): List<SwarmEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSwarmEvent(event: SwarmEntity)

  @Update
  suspend fun updateSwarmEvent(event: SwarmEntity)

  @Query("DELETE FROM swarms WHERE id = :id")
  suspend fun deleteSwarmEventById(id: String)

  @Query("DELETE FROM swarms")
  suspend fun clearAllSwarmEvents()
}
