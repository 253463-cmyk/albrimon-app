package com.albrimon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.albrimon.app.data.local.entity.ScrapEntity
import com.albrimon.app.data.local.entity.UsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ScrapDao {
    @Query("SELECT * FROM scraps ORDER BY available DESC, id DESC")
    abstract fun observeAll(): Flow<List<ScrapEntity>>

    @Query("SELECT * FROM scraps WHERE id = :id")
    abstract suspend fun get(id: Long): ScrapEntity?

    @Upsert
    abstract suspend fun upsert(scrap: ScrapEntity)

    @Upsert
    abstract suspend fun upsertAll(scraps: List<ScrapEntity>)

    @Query("DELETE FROM scraps WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("DELETE FROM scraps")
    abstract suspend fun clear()

    /** El servidor es la fuente de verdad: reemplaza la copia local completa en una transacción. */
    @Transaction
    open suspend fun replaceAll(scraps: List<ScrapEntity>) {
        clear()
        upsertAll(scraps)
    }
}

@Dao
abstract class UsageDao {
    @Query("SELECT * FROM scrap_usage WHERE projectId = :projectId ORDER BY id DESC")
    abstract fun observeByProject(projectId: Long): Flow<List<UsageEntity>>

    @Query("DELETE FROM scrap_usage WHERE projectId = :projectId")
    abstract suspend fun deleteByProject(projectId: Long)

    @Query("DELETE FROM scrap_usage")
    abstract suspend fun clear()

    @Upsert
    abstract suspend fun upsertAll(items: List<UsageEntity>)

    @Transaction
    open suspend fun replaceForProject(projectId: Long, items: List<UsageEntity>) {
        deleteByProject(projectId)
        upsertAll(items)
    }
}
