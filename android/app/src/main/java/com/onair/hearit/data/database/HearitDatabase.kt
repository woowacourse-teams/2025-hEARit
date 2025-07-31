package com.onair.hearit.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentHearitEntity::class, SearchHistoryEntity::class], version = 1)
abstract class HearitDatabase : RoomDatabase() {
    abstract fun hearitDao(): HearitDao

    companion object {
        private const val DB_NAME = "recent_hearit"

        @Volatile
        private var instance: HearitDatabase? = null

        fun getInstance(context: Context): HearitDatabase =
            instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }

        private fun createDatabase(context: Context): HearitDatabase =
            Room
                .databaseBuilder(
                    context.applicationContext,
                    HearitDatabase::class.java,
                    DB_NAME,
                ).build()
    }
}
