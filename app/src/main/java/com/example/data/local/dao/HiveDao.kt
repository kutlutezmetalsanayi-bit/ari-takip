package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.HiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
  @Query("SELECT * FROM hives WHERE status = 'active' ORDER BY hiveNumber ASC")
  fun getActiveHives(): Flow<List<HiveEntity>>

  @Query("SELECT * FROM hives ORDER BY hiveNumber ASC")
  fun getAllHives(): Flow<List<HiveEntity>>

  @Query("SELECT * FROM hives WHERE apiaryId = :apiaryId AND status = 'active' ORDER BY hiveNumber ASC")
  fun getActiveHivesByApiary(apiaryId: String): Flow<List<HiveEntity>>

  @Query("SELECT * FROM hives WHERE apiaryId = :apiaryId ORDER BY hiveNumber ASC")
  fun getAllHivesByApiary(apiaryId: String): Flow<List<HiveEntity>>

  @Query("SELECT * FROM hives WHERE id = :id")
  fun getHiveById(id: String): Flow<HiveEntity?>

  @Query("SELECT * FROM hives WHERE id = :id")
  suspend fun getHiveByIdSync(id: String): HiveEntity?

  @Query("SELECT * FROM hives ORDER BY hiveNumber ASC")
  suspend fun getAllHivesSync(): List<HiveEntity>

  // Atomic hive numbering per apiary: Max hive number ever created for this apiary
  @Query("SELECT MAX(hiveNumber) FROM hives WHERE apiaryId = :apiaryId")
  suspend fun getMaxHiveNumberForApiary(apiaryId: String): Int?

  // Global max fallback if needed
  @Query("SELECT MAX(hiveNumber) FROM hives")
  suspend fun getGlobalMaxHiveNumber(): Int?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHive(hive: HiveEntity)

  @Update
  suspend fun updateHive(hive: HiveEntity)

  @Query("UPDATE hives SET status = 'archived', updatedAt = :timestamp WHERE id = :id")
  suspend fun archiveHive(id: String, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE hives SET status = 'active', updatedAt = :timestamp WHERE id = :id")
  suspend fun unarchiveHive(id: String, timestamp: Long = System.currentTimeMillis())

  @Query("DELETE FROM hives WHERE id = :id")
  suspend fun deleteHiveById(id: String)

  @Query("DELETE FROM hives")
  suspend fun clearAllHives()
}
