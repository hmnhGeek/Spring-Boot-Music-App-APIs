package com.musicapp.music_app.controllers;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.requests.AddSongToPlaylistRequestDTO;
import DTOs.requests.PasswordRequestDTO;
import DTOs.responses.PlaylistResponseDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.DTO.Requests.User.CreateUserRequestDTO;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.services.CredentialService;
//import com.musicapp.music_app.services.PlaylistService;
import com.musicapp.music_app.services.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
    @PostMapping(value = "/create-playlist", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestParam("title") String title,
                                        @RequestParam("description") String description,
                                        @RequestPart("coverImagePath") MultipartFile coverImagePath) {
        try {
            AddPlaylistRequestDTO playlist = new AddPlaylistRequestDTO();
            playlist.setTitle(title);
            playlist.setDescription(description);
            Playlist savedPlaylist = playlistService.createPlaylist(playlist, coverImagePath);
            return new ResponseEntity<>(savedPlaylist, HttpStatus.CREATED);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Add song to a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Song added successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while adding the song")
    })
    @PostMapping(value = "/add-to-playlist", produces = MediaType.APPLICATION_JSON_VALUE)
    public void addSongToPlaylist(@RequestBody AddSongToPlaylistRequestDTO addSongToPlaylistRequestDTO, String playlistId) {
        List<String> songIds = addSongToPlaylistRequestDTO.getSongIds();
        songIds.forEach(songId -> playlistService.addSongToPlaylist(songId, playlistId));
    }

//    @Operation(summary = "Get all playlists")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Playlists fetched successfully"),
//            @ApiResponse(responseCode = "500", description = "Internal server error while fetching playlists")
//    })
//    @GetMapping("")
//    public ResponseEntity<List<PlaylistResponseDTO>> getAllPlaylists(@RequestParam(defaultValue = "") String password, @RequestParam Boolean isProtected) {
//        PasswordRequestDTO passwordRequestDTO = new PasswordRequestDTO();
//        passwordRequestDTO.setEncodedPassword(password);
//        if (isProtected && !credentialService.isValidPassword(passwordRequestDTO)) {
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//        List<PlaylistResponseDTO> playlists = playlistService.getAllPlaylists(isProtected);
//        return ResponseEntity.ok().body(playlists);
//    }
//
//    @Operation(summary = "Get all songs in a playlist")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Fetched songs successfully"),
//            @ApiResponse(responseCode = "500", description = "Internal server error while fetching songs")
//    })
//    @GetMapping("/get-songs")
//    public ResponseEntity<List<SongsListItem>> getAllSongsInPlaylist(@RequestParam(defaultValue = "") String password, @RequestParam String playlistId) {
//        List<SongsListItem> songs = playlistService.getAllSongsInPlaylists(playlistId, password);
//        return ResponseEntity.ok().body(songs);
//    }
//
//    @Operation(summary = "Remove a song from a playlist")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Song removed successfully"),
//            @ApiResponse(responseCode = "500", description = "Internal server error while removing song")
//    })
//    @DeleteMapping("/remove-song/{playlistId}/{songId}")
//    public Playlist removeSongFromPlaylist(
//            @RequestParam(defaultValue = "") String password,
//            @PathVariable String playlistId,
//            @PathVariable String songId) {
//        return playlistService.removeSongFromPlaylist(playlistId, songId, password);
//    }
//
//    @Operation(summary = "Remove a playlist")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Playlist deleted successfully"),
//            @ApiResponse(responseCode = "500", description = "Internal server error while deleting playlist")
//    })
//    @DeleteMapping("/delete-playlist/{playlistId}")
//    public void deletePlaylist(
//            @RequestParam(defaultValue = "") String password,
//            @PathVariable String playlistId
//    ) {
//        playlistService.deletePlaylist(playlistId, password);
//    }
}
