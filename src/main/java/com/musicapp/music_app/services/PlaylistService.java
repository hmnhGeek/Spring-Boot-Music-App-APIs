package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.requests.AddSongToPlaylistRequestDTO;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.repositories.PlaylistRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;

    public Playlist createPlaylist(AddPlaylistRequestDTO addPlaylistRequestDTO) {
        String name = addPlaylistRequestDTO.getPlaylistName();
        List<String> songIds = new ArrayList<>();
        Playlist playlist = new Playlist(name, songIds);
        return playlistRepository.save(playlist);
    }

    public Playlist addSongToPlaylist(AddSongToPlaylistRequestDTO addSongToPlaylistRequestDTO) {
        String playlistId = addSongToPlaylistRequestDTO.getPlaylistId();
        String songId = addSongToPlaylistRequestDTO.getSongId();

        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);

        if (playlistOptional.isPresent()) {
            Playlist playlist = playlistOptional.get();

            // Check if the song is already in the playlist
            if (!playlist.getSongIds().contains(songId)) {
                // Add songId to the list of songs in the playlist if not already present
                playlist.getSongIds().add(songId);
                // Save the updated playlist back to the database
                return playlistRepository.save(playlist);
            } else {
                // Song is already in the playlist, no need to add again
                throw new RuntimeException("Song already present in the playlist.");
            }
        } else {
            throw new RuntimeException("Playlist not found with id: " + playlistId);
        }
    }

}
