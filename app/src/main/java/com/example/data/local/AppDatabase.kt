package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ApiaryDao
import com.example.data.local.dao.FeedingDao
import com.example.data.local.dao.HiveDao
import com.example.data.local.dao.InspectionDao
import com.example.data.local.dao.IssueDao
import com.example.data.local.dao.PhotoDao
import com.example.data.local.dao.QueenDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.SwarmDao
import com.example.data.local.dao.SyncDao
import com.example.data.local.entity.ApiaryEntity
import com.example.data.local.entity.FeedingEntity
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.InspectionEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.PhotoEntity
import com.example.data.local.entity.QueenEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.SwarmEntity
import com.example.data.local.entity.SyncEntity

@Database(
  entities = [
    ApiaryEntity::class,
    HiveEntity::class,
    InspectionEntity::class,
    FeedingEntity::class,
    PhotoEntity::class,
    ReminderEntity::class,
    SyncEntity::class,
    QueenEntity::class,
    SwarmEntity::class,
    IssueEntity::class
  ],
  version = 4,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun apiaryDao(): ApiaryDao
  abstract fun hiveDao(): HiveDao
  abstract fun inspectionDao(): InspectionDao
  abstract fun feedingDao(): FeedingDao
  abstract fun photoDao(): PhotoDao
  abstract fun reminderDao(): ReminderDao
  abstract fun syncDao(): SyncDao
  abstract fun queenDao(): QueenDao
  abstract fun swarmDao(): SwarmDao
  abstract fun issueDao(): IssueDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `sync_queue` (
            `id` TEXT NOT NULL,
            `entityType` TEXT NOT NULL,
            `operation` TEXT NOT NULL,
            `recordId` TEXT NOT NULL,
            `payloadJson` TEXT NOT NULL,
            `createdAt` INTEGER NOT NULL,
            `retryCount` INTEGER NOT NULL,
            `status` TEXT NOT NULL,
            `lastError` TEXT,
            PRIMARY KEY(`id`)
          )
          """.trimIndent()
        )
      }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `queens` (
            `id` TEXT NOT NULL,
            `userId` TEXT NOT NULL,
            `hiveId` TEXT NOT NULL,
            `apiaryId` TEXT NOT NULL,
            `status` TEXT NOT NULL,
            `year` INTEGER NOT NULL,
            `breed` TEXT NOT NULL,
            `markingColor` TEXT NOT NULL,
            `source` TEXT NOT NULL,
            `installedDate` INTEGER NOT NULL,
            `notes` TEXT NOT NULL,
            `isActive` INTEGER NOT NULL,
            `createdAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
          """.trimIndent()
        )
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `swarms` (
            `id` TEXT NOT NULL,
            `userId` TEXT NOT NULL,
            `hiveId` TEXT NOT NULL,
            `apiaryId` TEXT NOT NULL,
            `eventType` TEXT NOT NULL,
            `eventDate` INTEGER NOT NULL,
            `tendencyLevel` TEXT NOT NULL,
            `queenCellsStatus` TEXT NOT NULL,
            `relatedHiveId` TEXT,
            `relatedHiveNumber` INTEGER,
            `actionTaken` TEXT NOT NULL,
            `notes` TEXT NOT NULL,
            `photoUri` TEXT,
            `createdAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
          """.trimIndent()
        )
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `issues` (
            `id` TEXT NOT NULL,
            `userId` TEXT NOT NULL,
            `hiveId` TEXT NOT NULL,
            `apiaryId` TEXT NOT NULL,
            `category` TEXT NOT NULL,
            `severity` TEXT NOT NULL,
            `status` TEXT NOT NULL,
            `detectedDate` INTEGER NOT NULL,
            `resolvedDate` INTEGER,
            `treatmentNotes` TEXT NOT NULL,
            `notes` TEXT NOT NULL,
            `photoUris` TEXT NOT NULL,
            `createdAt` INTEGER NOT NULL,
            `updatedAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
          """.trimIndent()
        )
      }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `apiaries` ADD COLUMN `country` TEXT NOT NULL DEFAULT 'Türkiye'")
        db.execSQL("ALTER TABLE `apiaries` ADD COLUMN `city` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `apiaries` ADD COLUMN `district` TEXT NOT NULL DEFAULT ''")
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "ari_takip_database"
        )
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
