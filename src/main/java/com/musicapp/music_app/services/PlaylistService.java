package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.repositories.PlaylistRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;

    public Playlist createPlaylist(AddPlaylistRequestDTO addPlaylistRequestDTO) {
        String name = addPlaylistRequestDTO.getPlaylistName();
        List<ObjectId> songIds = new ArrayList<>();
        Playlist playlist = new Playlist(name, songIds);
        return playlistRepository.save(playlist);
    }
}
