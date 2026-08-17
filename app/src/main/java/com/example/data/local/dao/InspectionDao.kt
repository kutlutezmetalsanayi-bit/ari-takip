package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
  @Query("SELECT * FROM inspections ORDER BY inspectionDate DESC")
  fun getAllInspections(): Flow<List<InspectionEntity>>

  @Query("SELECT * FROM inspections ORDER BY inspectionDate DESC")
  suspend fun getAllInspectionsSync(): List<InspectionEntity>

  @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY inspectionDate DESC")
  fun getInspectionsForHive(hiveId: String): Flow<List<InspectionEntity>>

  @Query("SELECT * FROM inspections WHERE apiaryId = :apiaryId ORDER BY inspectionDate DESC")
  fun getInspectionsForApiary(apiaryId: String): Flow<List<InspectionEntity>>

  @Query("SELECT * FROM inspections WHERE id = :id")
  suspend fun getInspectionById(id: String): InspectionEntity?

  @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY inspectionDate DESC LIMIT 1")
  fun getLatestInspectionForHive(hiveId: String): Flow<InspectionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInspection(inspection: InspectionEntity)

  @Update
  suspend fun updateInspection(inspection: InspectionEntity)

  @Query("DELETE FROM inspections WHERE id = :id")
  suspend fun deleteInspectionById(id: String)

  @Query("DELETE FROM inspections")
  suspend fun clearAllInspections()
}
