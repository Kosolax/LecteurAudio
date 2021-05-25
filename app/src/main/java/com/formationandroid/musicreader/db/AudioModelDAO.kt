package com.formationandroid.musicreader.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class AudioModelDAO {
    @Query("SELECT * FROM audioModels")
    abstract fun getListAudioModels(): List<AudioModelDTO>

    @Insert
    abstract fun insert(vararg audioModel: AudioModelDTO)

    @Query("SELECT COUNT(*) FROM audioModels WHERE uri = :uri")
    abstract fun countAudioModelByURI(uri: String): Long
}