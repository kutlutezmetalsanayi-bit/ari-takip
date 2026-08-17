package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.IssueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {
  @Query("SELECT * FROM issues WHERE hiveId = :hiveId ORDER BY detectedDate DESC")
  fun getIssuesForHive(hiveId: String): Flow<List<IssueEntity>>

  @Query("SELECT * FROM issues WHERE hiveId = :hiveId AND status != 'Çözüldü' ORDER BY detectedDate DESC")
  fun getActiveIssuesForHive(hiveId: String): Flow<List<IssueEntity>>

  @Query("SELECT * FROM issues ORDER BY detectedDate DESC")
  fun getAllIssues(): Flow<List<IssueEntity>>

  @Query("SELECT * FROM issues ORDER BY detectedDate DESC")
  suspend fun getAllIssuesSync(): List<IssueEntity>

  @Query("SELECT * FROM issues WHERE status != 'Çözüldü' ORDER BY detectedDate DESC")
  fun getAllActiveIssues(): Flow<List<IssueEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIssue(issue: IssueEntity)

  @Update
  suspend fun updateIssue(issue: IssueEntity)

  @Query("UPDATE issues SET status = :status, resolvedDate = :resolvedDate, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateIssueStatus(id: String, status: String, resolvedDate: Long?, updatedAt: Long = System.currentTimeMillis())

  @Query("DELETE FROM issues WHERE id = :id")
  suspend fun deleteIssueById(id: String)

  @Query("DELETE FROM issues")
  suspend fun clearAllIssues()
}
