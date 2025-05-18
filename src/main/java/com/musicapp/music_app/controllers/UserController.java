package com.musicapp.music_app.controllers;

import com.musicapp.music_app.DTO.Response.User.UserMetaDetailsDTO;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Retrieve and decrypt a profile image of a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile image retrieved and decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Profile image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while decrypting the profile image")
    })
    @GetMapping(value = "/profile-image", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getUserProfileImage() {
        try {
            // Delegate to service for retrieving and decrypting the profile image
            HashMap<String, Object> decryptedProfileImage = userService.getDecryptedProfileImage();

            if (decryptedProfileImage == null) {
                return new ResponseEntity<>("Profile image not found or decryption failed", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedProfileImage.get("filename") + "\"") // Set appropriate filename here
                    .body(decryptedProfileImage.get("file"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve user details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details extracted successfully"),
            @ApiResponse(responseCode = "404", description = "User details not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while finding user details")
    })
    @GetMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserMetaDetails() {
        try {
            UserMetaDetailsDTO response = userService.getUserMetaDetails();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve users having non-empty playlists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details extracted successfully"),
            @ApiResponse(responseCode = "404", description = "User details not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while finding user details")
    })
    @GetMapping(value = "/users-having-playlists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsersHavingPlaylists() {
        try {
            List<User> users = userService.getUsersHavingPlaylists();
            if (users == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return ResponseEntity.ok().body(users);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
