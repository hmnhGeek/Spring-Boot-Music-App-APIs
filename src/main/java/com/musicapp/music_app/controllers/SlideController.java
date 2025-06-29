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
    @GetMapping(value = "/{songId}")
    public ResponseEntity<List<String>> getSlidesForSong(@PathVariable("songId") String songId) {
        try {
            List<String> response = slidesService.getSlideImagesForSong(songId);
            if (response == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Associate a list of slides to a song by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slides linked to song successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error while linking slides to song")
    })
    @PostMapping("/{songId}")
    public ResponseEntity<String> addSlidesToSong(
            @PathVariable("songId") String songId,
            @RequestBody List<String> slideIds
    ) {
        try {
            if (slideIds == null || slideIds.isEmpty()) {
                return new ResponseEntity<>("Slide ID list cannot be empty", HttpStatus.BAD_REQUEST);
            }

            int i = slidesService.addExistingSlidesToSong(songId, slideIds);
            if (i == 1) return new ResponseEntity<>("Slides added to song successfully", HttpStatus.OK);
            if (i == 0) return new ResponseEntity<>("Song not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            System.out.println("Error while adding slides to song: " + e.getMessage());
            return new ResponseEntity<>("Failed to add slides to song", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Add new slides (by URLs) to a song and associate them with the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New slides created and added to song"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or song ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error while creating slides")
    })
    @PostMapping("add-new-slides/{songId}")
    public ResponseEntity<String> addNewSlidesToSong(
            @PathVariable("songId") String songId,
            @RequestBody List<String> urls
    ) {
        try {
            if (urls == null || urls.isEmpty()) {
                return new ResponseEntity<>("Slide URL list cannot be empty", HttpStatus.BAD_REQUEST);
            }

            int i = slidesService.addNewSlidesToSong(songId, urls);
            if (i == 1) return new ResponseEntity<>("Slides added to song successfully", HttpStatus.OK);
            if (i == 0) return new ResponseEntity<>("Song not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Add new slides (URLs) to the current user's collection.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slides added successfully"),
            @ApiResponse(responseCode = "400", description = "No URLs provided"),
            @ApiResponse(responseCode = "500", description = "Error while adding slides")
    })
    @PostMapping("/add-new-slides")
    public ResponseEntity<String> addSlidesByUrls(@RequestBody List<String> urls) {
        try {
            if (urls == null || urls.isEmpty()) {
                return new ResponseEntity<>("No slide URLs provided", HttpStatus.BAD_REQUEST);
            }

            slidesService.addSlidesByUrls(urls);
            return new ResponseEntity<>("Slides added successfully", HttpStatus.CREATED);

        } catch (Exception e) {
            System.out.println("Error while adding slides: " + e.getMessage());
            return new ResponseEntity<>("Error while adding slides", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete a slide by ID and remove it from the current user's collection.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slide deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Slide or user not found"),
            @ApiResponse(responseCode = "500", description = "Error deleting slide")
    })
    @DeleteMapping("/{slideId}")
    public ResponseEntity<String> deleteSlideById(@PathVariable("slideId") String slideId) {
        try {
            slidesService.deleteSlideById(slideId);
            return new ResponseEntity<>("Slide deleted successfully", HttpStatus.OK);

        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("not found")) {
                return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
            }
            System.out.println("Error while deleting slide: " + message);
            return new ResponseEntity<>("Error deleting slide", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Remove specified slides from a song.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slides removed from song successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Error while removing slides from song")
    })
    @PostMapping("remove-slides/{songId}")
    public ResponseEntity<String> removeSlidesFromSong(
            @PathVariable("songId") String songId,
            @RequestBody List<String> slideIds
    ) {
        try {
            if (slideIds == null || slideIds.isEmpty()) {
                return new ResponseEntity<>("Slide ID list cannot be empty", HttpStatus.BAD_REQUEST);
            }

            slidesService.removeSlidesFromSong(songId, slideIds);
            return new ResponseEntity<>("Slides removed from song successfully", HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error removing slides from song: " + e.getMessage());
            return new ResponseEntity<>("Error while removing slides from song", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Fetch all decrypted slide URLs for the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved decrypted slide URLs"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving slides")
    })
    @GetMapping
    public ResponseEntity<List<String>> getUserSlides() {
        try {
            List<String> decryptedUrls = slidesService.getDecryptedSlideUrlsForCurrentUser();
            return ResponseEntity.ok(decryptedUrls);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            System.out.println("Error fetching user slides: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
