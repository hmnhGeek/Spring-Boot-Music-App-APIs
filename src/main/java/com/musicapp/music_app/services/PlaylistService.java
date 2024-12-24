package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.requests.AddSongToPlaylistRequestDTO;
import DTOs.requests.PasswordRequestDTO;
import DTOs.responses.PlaylistResponseDTO;
import DTOs.responses.SongsListItem;
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

    @Autowired
    private CredentialService credentialService;

    private boolean unauthorizedAccessToPlaylist(String playlistId, String encodedPassword) {
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if (optionalPlaylist.isEmpty()) {
            return true;
        }
        Playlist playlist = optionalPlaylist.get();
        PasswordRequestDTO passwordRequestDTO = new PasswordRequestDTO();
        passwordRequestDTO.setEncodedPassword(encodedPassword);
        return playlist.isProtectedPlaylist() && !credentialService.isValidPassword(passwordRequestDTO);
    }

    public Playlist createPlaylist(AddPlaylistRequestDTO addPlaylistRequestDTO) {
        String name = addPlaylistRequestDTO.getPlaylistName();
        List<String> songIds = new ArrayList<>();
        Boolean isProtected = addPlaylistRequestDTO.getIsProtected();
        Playlist playlist = new Playlist(name, songIds, isProtected);
        return playlistRepository.save(playlist);
    }

    public Playlist addSongToPlaylist(AddSongToPlaylistRequestDTO addSongToPlaylistRequestDTO) {
        String playlistId = addSongToPlaylistRequestDTO.getPlaylistId();
        String encodedPassword = addSongToPlaylistRequestDTO.getPassword();

        if(unauthorizedAccessToPlaylist(playlistId, encodedPassword)) {
            throw new RuntimeException("User not authorized to view playlist with ID: " + playlistId);
        }

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

    public List<SongsListItem> getAllSongsInPlaylists(String playlistId, String password) {
        if(unauthorizedAccessToPlaylist(playlistId, password)) {
            throw new RuntimeException("User not authorized to view playlist with ID: " + playlistId);
        }

        // Fetch the playlist by its ID
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);

        if (!playlistOptional.isPresent()) {
            throw new RuntimeException("Playlist not found with id: " + playlistId);
        }

        Playlist playlist = playlistOptional.get();
        List<String> songIds = playlist.getSongIds();

        // Remove stale song IDs that don't exist in the system
        List<String> validSongIds = songIds.stream()
                .filter(songId -> songRepository.findById(songId).isPresent())  // Only keep valid IDs
                .collect(Collectors.toList());

        // Update the playlist with the valid song IDs
        playlist.setSongIds(validSongIds);
        playlistRepository.save(playlist);  // Save the updated playlist

        // Fetch the valid songs from the repository

        return validSongIds.stream().map(songId -> {
            Optional<Song> song = songRepository.findById(songId);
            SongsListItem item = new SongsListItem();
            song.ifPresent(value -> {
                item.setOriginalName(value.getOriginalName());
                item.setId(value.getId());
            });
            return item;
        }).collect(Collectors.toList());
    }


    public Playlist removeSongFromPlaylist(String playlistId, String songId, String password) {
        if (unauthorizedAccessToPlaylist(playlistId, password)) {
            throw new RuntimeException("User not authorized to view playlist with ID: " + playlistId);
        }

        // Fetch the playlist by its ID
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);

        if (!playlistOptional.isPresent()) {
            throw new RuntimeException("Playlist not found with id: " + playlistId);
        }

        Playlist playlist = playlistOptional.get();
        List<String> songIds = playlist.getSongIds();

        // Check if the songId exists in the playlist
        if (songIds.contains(songId)) {
            // Remove the songId from the playlist
            songIds.remove(songId);

            // Update the playlist and save it
            playlist.setSongIds(songIds);
            return playlistRepository.save(playlist);
        } else {
            throw new RuntimeException("Song with id " + songId + " not found in the playlist.");
        }
    }

    public void deletePlaylist(String playlistId, String password) {
        if (unauthorizedAccessToPlaylist(playlistId, password)) {
            throw new RuntimeException("User not authorized to delete playlist with ID: " + playlistId);
        }

        // Check if the playlist exists
        Optional<Playlist> playlistOptional = playlistRepository.findById(playlistId);
        if (!playlistOptional.isPresent()) {
            throw new RuntimeException("Playlist not found with id: " + playlistId);
        }

        // Remove the playlist
        playlistRepository.deleteById(playlistId);
    }
}
