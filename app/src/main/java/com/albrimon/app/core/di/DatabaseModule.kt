package com.albrimon.app.core.di

import android.content.Context
import androidx.room.Room
import com.albrimon.app.data.local.AlbrimonDatabase
import com.albrimon.app.data.local.dao.ScrapDao
import com.albrimon.app.data.local.dao.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt: cómo crear la base de datos Room y sus DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlbrimonDatabase =
        Room.databaseBuilder(context, AlbrimonDatabase::class.java, "albrimon.db")
            // Es una copia del servidor: si cambia el esquema se vuelve a descargar.
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideScrapDao(db: AlbrimonDatabase): ScrapDao = db.scrapDao()

    @Provides
    fun provideUsageDao(db: AlbrimonDatabase): UsageDao = db.usageDao()
}
