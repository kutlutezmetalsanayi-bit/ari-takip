package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QueenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueenDao {
  @Query("SELECT * FROM queens WHERE hiveId = :hiveId AND isActive = 1 LIMIT 1")
  fun getActiveQueenForHive(hiveId: String): Flow<QueenEntity?>

  @Query("SELECT * FROM queens WHERE hiveId = :hiveId AND isActive = 1 LIMIT 1")
  suspend fun getActiveQueenForHiveSync(hiveId: String): QueenEntity?

  @Query("SELECT * FROM queens WHERE hiveId = :hiveId ORDER BY installedDate DESC")
  fun getQueenHistoryForHive(hiveId: String): Flow<List<QueenEntity>>

  @Query("SELECT * FROM queens ORDER BY installedDate DESC")
  fun getAllQueens(): Flow<List<QueenEntity>>

  @Query("SELECT * FROM queens ORDER BY installedDate DESC")
  suspend fun getAllQueensSync(): List<QueenEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQueen(queen: QueenEntity)

  @Update
  suspend fun updateQueen(queen: QueenEntity)

  @Query("UPDATE queens SET isActive = 0 WHERE hiveId = :hiveId")
  suspend fun deactivateQueensForHive(hiveId: String)

  @Query("DELETE FROM queens WHERE id = :id")
  suspend fun deleteQueenById(id: String)

  @Query("DELETE FROM queens")
  suspend fun clearAllQueens()
}
