package com.uvg.labmusic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MusicDao {

    @Query("SELECT * FROM songs ORDER BY isFavorite DESC, name ASC")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM artists")
    suspend fun getAllArtists(): List<Artist>

    @Insert
    suspend fun insertArtist(artist: Artist)

    @Insert
    suspend fun insertSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: Int, isFavorite: Boolean)

    @Update
    suspend fun updateSong(song: Song)
}