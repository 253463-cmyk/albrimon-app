package com.albrimon.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.albrimon.app.data.local.dao.ScrapDao
import com.albrimon.app.data.local.dao.UsageDao
import com.albrimon.app.data.local.entity.ScrapEntity
import com.albrimon.app.data.local.entity.UsageEntity

/** Base de datos local (SQLite con Room). */
@Database(entities = [ScrapEntity::class, UsageEntity::class], version = 1, exportSchema = false)
abstract class AlbrimonDatabase : RoomDatabase() {
    abstract fun scrapDao(): ScrapDao
    abstract fun usageDao(): UsageDao
}
