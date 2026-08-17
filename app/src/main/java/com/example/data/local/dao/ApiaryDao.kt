package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ApiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {
  @Query("SELECT * FROM apiaries WHERE isActive = 1 ORDER BY createdAt ASC")
  fun getActiveApiaries(): Flow<List<ApiaryEntity>>

  @Query("SELECT * FROM apiaries ORDER BY createdAt ASC")
  fun getAllApiaries(): Flow<List<ApiaryEntity>>

  @Query("SELECT * FROM apiaries WHERE id = :id")
  fun getApiaryById(id: String): Flow<ApiaryEntity?>

  @Query("SELECT * FROM apiaries WHERE id = :id")
  suspend fun getApiaryByIdSync(id: String): ApiaryEntity?

  @Query("SELECT * FROM apiaries ORDER BY createdAt ASC")
  suspend fun getAllApiariesSync(): List<ApiaryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertApiary(apiary: ApiaryEntity)

  @Update
  suspend fun updateApiary(apiary: ApiaryEntity)

  @Query("DELETE FROM apiaries WHERE id = :id")
  suspend fun deleteApiaryById(id: String)

  @Query("DELETE FROM apiaries")
  suspend fun clearAllApiaries()
}
