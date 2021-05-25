package com.formationandroid.musicreader.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AudioModelDTO::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun audioModelsDAO(): AudioModelDAO
}