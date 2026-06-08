package com.yslee.subwaywhen.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShinbundangScheduleDao {
    @Query("SELECT * FROM shinbundang_schedule WHERE stationName = :stationName LIMIT 1")
    suspend fun load(stationName: String): ShinbundangScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ShinbundangScheduleEntity)

    @Query("DELETE FROM shinbundang_schedule WHERE stationName = :stationName")
    suspend fun delete(stationName: String)
}
