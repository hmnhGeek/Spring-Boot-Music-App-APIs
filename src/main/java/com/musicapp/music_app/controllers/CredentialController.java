package com.musicapp.music_app.controllers;

import DTOs.requests.PasswordRequestDTO;
import com.musicapp.music_app.model.Credential;
import com.musicapp.music_app.repositories.CredentialRepository;
import com.musicapp.music_app.services.CredentialService;
import com.musicapp.music_app.utils.EncryptionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/credentials")
@CrossOrigin(origins = "*")
public class CredentialController {

    @Autowired
    private CredentialService credentialService;

    @PostMapping("/validate")
    public ResponseEntity<String> checkPassword(@RequestBody PasswordRequestDTO passwordRequest) {
        try {
            boolean isValidPassword = credentialService.isValidPassword(passwordRequest);
            if(isValidPassword) {
                return ResponseEntity.ok("Password is correct.");
            }
            return ResponseEntity.status(401).body("Incorrect password.");
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
