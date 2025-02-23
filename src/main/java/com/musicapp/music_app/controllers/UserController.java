package com.musicapp.music_app.controllers;

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

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Retrieve and decrypt a profile image by its user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile image retrieved and decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Profile image not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while decrypting the profile image")
    })
    @GetMapping(value = "/profile-image/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getUserProfileImage(@PathVariable("id") String userId) {
        try {
            // Delegate to service for retrieving and decrypting the profile image
            HashMap<String, Object> decryptedProfileImage = userService.getDecryptedProfileByUserId(userId);

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
}
