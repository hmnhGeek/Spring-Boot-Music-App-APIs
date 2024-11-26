package com.musicapp.music_app.controllers;

import DTOs.requests.SongUploadRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.services.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongService songService;

    @Operation(summary = "Upload and encrypt a song with cover image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Song uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while uploading the song")
    })
//    @CrossOrigin(origins = "*")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Song> uploadSong(
            @RequestPart("songFilePath") MultipartFile songFilePath,
            @RequestPart("coverImagePath") MultipartFile coverImagePath
    ) {
        try {
            // Call the service method to upload and encrypt the song and cover image
            Song savedSong = songService.uploadAndEncryptSong(songFilePath, coverImagePath);
            return new ResponseEntity<>(savedSong, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Handle exceptions (e.g., encryption, file saving issues)
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve and decrypt a song cover image by its MongoDB ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cover image retrieved and decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Cover image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while decrypting the cover image")
    })
    @GetMapping(value = "/get-song-cover-image/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getSongCoverImageByIdAndDecrypt(@PathVariable("id") String songId) {
        try {
            // Delegate to service for retrieving and decrypting the cover image
            HashMap<String, Object> decryptedMusicFile = songService.getDecryptedSongCoverById(songId);

            if (decryptedMusicFile == null) {
                return new ResponseEntity<>("Cover image not found or decryption failed", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedMusicFile.get("filename") + "\"") // Set appropriate filename here
                    .body(decryptedMusicFile.get("file"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve and decrypt a song by its MongoDB ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song retrieved and decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Song not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while decrypting the song")
    })
    @GetMapping(value = "/get-song/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getSongByIdAndDecrypt(@PathVariable("id") String songId) {
        try {
            // Delegate to service for retrieving and decrypting the song
            HashMap<String, Object> decryptedMusicFile = songService.getDecryptedSongById(songId);

            if (decryptedMusicFile == null) {
                return new ResponseEntity<>("Song not found or decryption failed", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedMusicFile.get("filename") + "\"") // Set appropriate filename here
                    .body(decryptedMusicFile.get("file"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve list of songs stored in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Song list not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching song list")
    })
    @GetMapping(value = "/get-song-list")
    public ResponseEntity<List<SongsListItem>> getSongsList() {
        try {
            List<SongsListItem> songsList = songService.getSongsList();
            if (songsList == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok().body(songsList);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
