package com.musicapp.music_app.controllers;

import com.musicapp.music_app.services.SlidesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slides")
@CrossOrigin(origins = "*")
public class SlideController {
    @Autowired
    private SlidesService slidesService;

    @Operation(summary = "Retrieve all image urls for a song's slides.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slides list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Slides list not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while fetching slides list")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<List<String>> getSlidesForSong(@PathVariable("id") String songId) {
        try {
            List<String> response = slidesService.getSlideImagesForSong(songId);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
