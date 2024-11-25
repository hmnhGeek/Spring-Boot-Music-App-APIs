package com.musicapp.music_app.controllers;

import DTOs.requests.SongUploadRequestDTO;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.services.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
            Song savedSong = songService.uploadAndEncryptSong(songFilePath.getInputStream(), coverImagePath.getInputStream());
            return new ResponseEntity<>(savedSong, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Handle exceptions (e.g., encryption, file saving issues)
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
