package com.yslee.subwaywhen.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shinbundang_schedule")
data class ShinbundangScheduleEntity(
    @PrimaryKey val stationName: String,
    val scheduleData: String,
    val scheduleVersion: String,
)
