package com.uvg.labmusic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uvg.labmusic.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = MusicDatabase.getDatabase(this)
        val dao = db.musicDao()

        lifecycleScope.launch {
            if (dao.getAllArtists().isEmpty()) {
                Log.d("MainActivity", "Base de datos vacía. Insertando datos iniciales...")
                val initialArtists = DataGenerator.getArtists()
                val initialSongs = DataGenerator.getSongs()

                initialArtists.forEach { artistDTO ->
                    dao.insertArtist(artistDTO.toEntity())
                }

                initialSongs.forEach { songDTO ->
                    dao.insertSong(songDTO.toEntity())
                }
                Log.d("MainActivity", "Datos iniciales insertados correctamente.")
            }
        }

        setContent {
            val viewModel: MusicViewModel = viewModel(factory = MusicViewModelFactory(dao))
            MusicApp(viewModel)
        }
    }
}

@Composable
fun MusicApp(viewModel: MusicViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            when (selectedTab) {
                0 -> ExploreScreen(viewModel)
                1 -> ArtistScreen(viewModel)
            }
        }
    }
}

class MusicViewModel(private val dao: MusicDao) : ViewModel() {
    var songList by mutableStateOf<List<Song>>(emptyList())
        private set

    var artistList by mutableStateOf<List<Artist>>(emptyList())
        private set

    init {
        loadSongs()
        loadArtists()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            songList = dao.getAllSongs()
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            artistList = dao.getAllArtists()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newFavoriteState = !song.isFavorite
            dao.setFavorite(song.id, newFavoriteState)
            loadSongs() // Recargar las canciones después de actualizar el favorito
        }
    }
}

class MusicViewModelFactory(private val dao: MusicDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "Artistas") },
            label = { Text("Artistas") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
    }
}

@Composable
fun ExploreScreen(viewModel: MusicViewModel) {
    SongList(
        songs = viewModel.songList,
        onFavoriteClick = { song -> viewModel.toggleFavorite(song) }
    )
}

@Composable
fun ArtistScreen(viewModel: MusicViewModel) {
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }

    if (selectedArtist == null) {
        ArtistList(
            artists = viewModel.artistList,
            onArtistClick = { artist -> selectedArtist = artist }
        )
    } else {
        ArtistDetailScreen(
            artist = selectedArtist,
            songs = viewModel.songList.filter { it.artist_id == selectedArtist?.id },
            onBackClick = { selectedArtist = null }, // Para regresar a la lista de artistas
            onFavoriteClick = { song -> viewModel.toggleFavorite(song) }
        )
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist?,
    songs: List<Song>,
    onBackClick: () -> Unit,
    onFavoriteClick: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackClick) {
            Text("Volver a Artistas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        artist?.let {
            Text(text = it.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Oyentes mensuales: ${it.monthlyListeners}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(songs) { song ->
                SongItem(song, onFavoriteClick)
            }
        }
    }
}

@Composable
fun SongList(songs: List<Song>, onFavoriteClick: (Song) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(songs) { song ->
            SongItem(song, onFavoriteClick)
        }
    }
}

@Composable
fun ArtistList(artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(artists) { artist ->
            ArtistItem(artist, onArtistClick)
        }
    }
}

@Composable
fun SongItem(song: Song, onFavoriteClick: (Song) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onFavoriteClick(song) }
            .background(if (song.isFavorite) Color(0xFFFFFDE7) else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = getUniqueColor(song.id), shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = song.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = song.genre, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(text = "Artista: ${song.artist_id}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (song.isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorito",
                tint = Color.Yellow
            )
        }
    }
}

@Composable
fun ArtistItem(artist: Artist, onArtistClick: (Artist) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onArtistClick(artist) }
    ) {
        Column {
            Text(text = artist.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Oyentes mensuales: ${artist.monthlyListeners}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun getUniqueColor(id: Int): Color {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Cyan, Color.Yellow)
    return colors[id % colors.size]
}