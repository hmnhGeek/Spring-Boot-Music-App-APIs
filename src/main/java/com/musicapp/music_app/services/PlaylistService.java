package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.requests.AddSongToPlaylistRequestDTO;
import DTOs.responses.PlaylistResponseDTO;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.repositories.PlaylistRepository;
import com.musicapp.music_app.repositories.SongRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    public Playlist createPlaylist(AddPlaylistRequestDTO addPlaylistRequestDTO) {
        String name = addPlaylistRequestDTO.getPlaylistName();
        List<String> songIds = new ArrayList<>();
        Boolean isProtected = addPlaylistRequestDTO.getIsProtected();
        Playlist playlist = new Playlist(name, songIds, isProtected);
        return playlistRepository.save(playlist);
    }

    public Playlist addSongToPlaylist(AddSongToPlaylistRequestDTO addSongToPlaylistRequestDTO) {
        String playlistId = addSongToPlaylistRequestDTO.getPlaylistId();
        String songId = addSongToPlaylistRequestDTO.getSongId();

        // Fetch the playlist by its ID
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);

        if (!playlistOptional.isPresent()) {
            throw new RuntimeException("Playlist not found with id: " + playlistId);
        }

        // Fetch the song by its ID (assuming you have a Song repository or service to fetch song details)
        Optional<Song> songOptional = songRepository.findById(songId);  // You need to create this logic
        if (!songOptional.isPresent()) {
            throw new RuntimeException("Song not found with id: " + songId);
        }

        Song song = songOptional.get();
        Playlist playlist = playlistOptional.get();

        // Check protection conditions
        if ((song.isVaultProtected() && !playlist.isProtectedPlaylist()) ||
                (!song.isVaultProtected() && playlist.isProtectedPlaylist())) {
            throw new RuntimeException("Cannot add this song to the playlist due to protection mismatch.");
        }

        // Check if the song is already in the playlist
        if (playlist.getSongIds().contains(songId)) {
            throw new RuntimeException("Song already present in the playlist.");
        }

        // Add songId to the list of songs in the playlist
        playlist.getSongIds().add(songId);

        // Save the updated playlist back to the database
        return playlistRepository.save(playlist);
    }


    public List<PlaylistResponseDTO> getAllPlaylists(Boolean protectedStatus) {
        List<Playlist> playlists;

        // If protectedStatus is null, fetch only playlists where protectedPlaylist is false
        if (protectedStatus == null) {
            playlists = playlistRepository.findByProtectedPlaylist(false);
        } else {
            // Fetch playlists based on the provided protectedStatus (true or false)
            playlists = playlistRepository.findByProtectedPlaylist(protectedStatus);
        }

        // Map the playlists to DTOs
        return playlists.stream()
                .map(playlist -> new PlaylistResponseDTO(playlist.getId(), playlist.getName(), playlist.isProtectedPlaylist()))
                .collect(Collectors.toList());
    }

}
