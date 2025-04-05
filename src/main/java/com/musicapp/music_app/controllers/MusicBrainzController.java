package com.musicapp.music_app.controllers;

import com.musicapp.music_app.DTO.Response.MusicBrainz.RecordingResponse;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.UserRepository;
import com.musicapp.music_app.services.MusicBrainzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/musicbrainz")
@CrossOrigin(origins = "*")
public class MusicBrainzController {
    @Autowired
    private MusicBrainzService musicBrainzService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get music meta data by title.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Song meta data fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching the metadata")
    })
    @GetMapping("/metadata")
    public ResponseEntity<RecordingResponse> getMusicMetaDataByTitle(@PathParam("title") String title) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        RecordingResponse response = musicBrainzService.getMetaDataByTitle(title);
        return ResponseEntity.ok().body(response);
    }
}
