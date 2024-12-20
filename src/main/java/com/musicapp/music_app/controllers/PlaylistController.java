package com.musicapp.music_app.controllers;

import DTOs.requests.AddPlaylistRequestDTO;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.services.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistController {
    @Autowired
    private PlaylistService playlistService;

    @Operation(summary = "Create new playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Playlist created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while creating the playlist")
    })
    @PostMapping(value = "/create-playlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Playlist> createPlaylist(
            @RequestBody AddPlaylistRequestDTO addPlaylistRequestDTO
    ) {
        try {
            // Call the service method to upload and encrypt the song and cover image
            Playlist playlist = playlistService.createPlaylist(addPlaylistRequestDTO);
            return new ResponseEntity<>(playlist, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
