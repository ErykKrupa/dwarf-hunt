package com.example.krasnalhunt.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [DwarfItem::class], version = 1)
@TypeConverters(value = [LatLngTypeConverter::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun dwarfItemDao(): DwarfItemDao

    companion object {
        @JvmStatic
        @get:Synchronized
        var instance: AppDatabase? = null
            private set

        @JvmStatic
        @Synchronized
        fun createInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, "main")
                    .build()
            }

            return instance!!
        }
    }
}