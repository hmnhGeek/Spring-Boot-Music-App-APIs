package com.musicapp.music_app.controllers;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.requests.AddSongToPlaylistRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.DTO.Response.Playlists.PlaylistResponseDTO;
import com.musicapp.music_app.model.Playlist;
//import com.musicapp.music_app.services.PlaylistService;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.services.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    @Operation(summary = "Remove a song from a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Song removed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while removing song")
    })
    @DeleteMapping("/remove-song/{playlistId}/{songId}")
    public void removeSongFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String songId) {
        playlistService.removeSongFromPlaylist(playlistId, songId);
    }

    @Operation(summary = "Get all playlists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched all playlists"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching playlists")
    })
    @GetMapping
    public List<PlaylistResponseDTO> getAllPlaylists() {
        List<Playlist> allPlaylists = playlistService.getAllPlaylists();
        return allPlaylists.stream().map(playlist -> {
            PlaylistResponseDTO item = new PlaylistResponseDTO();
            item.setId(playlist.getId().toString());
            item.setTitle(playlist.getTitle());
            item.setDescription(playlist.getDescription());
            item.setTotalSongs(playlist.getSongs().size());
            return item;
        }).toList();
    }

    @Operation(summary = "Delete a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Playlist deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting song")
    })
    @DeleteMapping("/{playlistId}")
    public void removePlaylist(@PathVariable String playlistId) {
        playlistService.removePlaylist(playlistId);
    }

    @Operation(summary = "Retrieve and decrypt playlist cover image by its MongoDB ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cover image retrieved and decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Cover image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while decrypting the cover image")
    })
    @GetMapping(value = "/cover-image/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getCoverImageByIdAndDecrypt(@PathVariable("id") String playlistId) {
        try {
            // Delegate to service for retrieving and decrypting the cover image
            HashMap<String, Object> decryptedCoverFile = playlistService.getDecryptedCoverById(playlistId);

            if (decryptedCoverFile == null) {
                return new ResponseEntity<>("Cover image not found or decryption failed", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedCoverFile.get("filename") + "\"") // Set appropriate filename here
                    .body(decryptedCoverFile.get("file"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve songs from a playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Songs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Songs not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving songs")
    })
    @GetMapping(value = "/songs/{playlistId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSongsFromPlaylist(@PathVariable("playlistId") String playlistId) {
        try {
            // Delegate to service for retrieving and decrypting the cover image
            List<SongsListItem> songs = playlistService.getSongs(playlistId);
            return ResponseEntity.ok().body(songs);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
