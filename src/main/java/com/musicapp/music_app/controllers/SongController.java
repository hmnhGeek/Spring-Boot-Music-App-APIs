package com.musicapp.music_app.controllers;

import DTOs.requests.PasswordRequestDTO;
import DTOs.requests.SongUploadRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.services.CredentialService;
import com.musicapp.music_app.services.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongService songService;

    @Autowired
    private CredentialService credentialService;

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

    @Operation(summary = "Retrieve paginated list of songs stored in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Song list not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching song list")
    })
    @GetMapping(value = "/get-song-list")
    public ResponseEntity<Map<String, Object>> getSongsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        try {
            // Create Pageable object
            Pageable pageable = PageRequest.of(page, size);

            // Fetch paginated song list
            Page<SongsListItem> songPage = songService.getSongsList(pageable);

            // Check if the page is empty
            if (songPage.getContent().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Prepare the response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("content", songPage.getContent());  // List of songs
            response.put("totalPages", songPage.getTotalPages());  // Total number of pages
            response.put("totalElements", songPage.getTotalElements());  // Total number of songs
            response.put("currentPage", songPage.getNumber());  // Current page index

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve paginated list of songs stored in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Song list not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching song list")
    })
    @GetMapping(value = "/get-song-list-lite")
    public ResponseEntity<List<SongsListItem>> getSongsListLite() {
        try {
            // Fetch paginated song list
            List<SongsListItem> songsList = songService.getSongsListWithoutCoverImages();

            // Check if the page is empty
            if (songsList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok().body(songsList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete a song by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Song not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting the song")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSongById(@PathVariable("id") String songId) {
        try {
            boolean isDeleted = songService.deleteSongById(songId);

            if (isDeleted) {
                return new ResponseEntity<>("Song deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Song not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update a song cover by song ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Song cover updated successfully"),
            @ApiResponse(responseCode = "404", description = "Song not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while updating the song cover")
    })
    @PutMapping(value = "/change-cover/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changeSongCover(@PathVariable("id") String songId, @RequestParam("coverImagePath") MultipartFile coverImagePath) {
        try {
            HashMap<String, Object> decryptedOldCoverImage = songService.changeSongCoverImage(songId, coverImagePath);
            if (decryptedOldCoverImage == null) {
                return new ResponseEntity<>("Cover image not found or decryption failed", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedOldCoverImage.get("filename") + "\"") // Set appropriate filename here
                    .body(decryptedOldCoverImage.get("file"));
        } catch (IllegalArgumentException e) {
            // Handle case where the song ID is invalid or update fails
            return new ResponseEntity<>("Song not found or update failed", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // Handle unexpected exceptions
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
