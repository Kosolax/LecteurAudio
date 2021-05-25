package com.formationandroid.musicreader.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audioModels")
class AudioModelDTO (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val size: Float,
    val duration: String,
    val uri: String
)