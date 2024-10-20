package com.uvg.labmusic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val artist_id: String,
    val genre: String,
    val duration: Int,
    val isFavorite: Boolean = false
)