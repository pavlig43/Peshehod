package ru.pavlig43.peshehod.core.database

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)

@Dao
interface AppMetadataDao {
    @Query("SELECT * FROM app_metadata WHERE `key` = :key LIMIT 1")
    suspend fun find(key: String): AppMetadataEntity?

    @Insert
    suspend fun insert(entry: AppMetadataEntity)
}

@Database(
    entities = [AppMetadataEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(PeshehodDatabaseConstructor::class)
abstract class PeshehodDatabase : RoomDatabase() {
    abstract fun metadataDao(): AppMetadataDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object PeshehodDatabaseConstructor : RoomDatabaseConstructor<PeshehodDatabase> {
    override fun initialize(): PeshehodDatabase
}
