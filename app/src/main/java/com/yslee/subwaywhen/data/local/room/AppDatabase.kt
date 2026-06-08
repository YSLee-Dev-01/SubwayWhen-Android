package com.yslee.subwaywhen.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ShinbundangScheduleEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shinbundangScheduleDao(): ShinbundangScheduleDao
}
