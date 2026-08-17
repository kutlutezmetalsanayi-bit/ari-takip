package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
  @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
  fun getPendingSyncItemsFlow(): Flow<List<SyncEntity>>

  @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
  suspend fun getPendingSyncItems(): List<SyncEntity>

  @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED'")
  fun getPendingSyncCountFlow(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSyncItem(item: SyncEntity)

  @Update
  suspend fun updateSyncItem(item: SyncEntity)

  @Query("DELETE FROM sync_queue WHERE id = :id")
  suspend fun deleteSyncItem(id: String)

  @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
  suspend fun clearSyncedItems()

  @Query("DELETE FROM sync_queue")
  suspend fun clearAll()
}
