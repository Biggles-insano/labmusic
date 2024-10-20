package com.uvg.labmusic.data

data class ArtistDTO(
    val id: String,
    val name: String,
    val monthlyListeners: Int,
    val album_count: Int
)

data class SongDTO(
    val id: Int,
    val name: String,
    val artist_id: String,
    val genre: String,
    val duration: Int
)

fun ArtistDTO.toEntity() = Artist(
    id = this.id,
    name = this.name,
    monthlyListeners = this.monthlyListeners,
    album_count = this.album_count
)

fun SongDTO.toEntity() = Song(
    id = 0, // Room generará automáticamente el ID
    name = this.name,
    artist_id = this.artist_id,
    genre = this.genre,
    duration = this.duration,
    isFavorite = false // Valor inicial por defecto
)

// Definición del generador de datos
object DataGenerator {
    fun getArtists(): List<ArtistDTO> = listOf(
        ArtistDTO("A", "Metallica", 8_234_567, 10),
        ArtistDTO("B", "Gojira", 1_234_567, 6),
        ArtistDTO("C", "Taylor Swift", 9_876_543, 9),
        // Otros artistas...
    )

    fun getSongs(): List<SongDTO> {
        var songId = 1
        return listOf(
            SongDTO(songId++, "Enter Sandman", "A", "Heavy Metal", 332),
            SongDTO(songId++, "Nothing Else Matters", "A", "Heavy Metal", 386),
            // Otras canciones...
        )
    }
}