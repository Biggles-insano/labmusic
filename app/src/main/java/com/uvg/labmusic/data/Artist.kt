package com.uvg.labmusic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val id: String,
    val name: String,
    val monthlyListeners: Int,
    val album_count: Int
)