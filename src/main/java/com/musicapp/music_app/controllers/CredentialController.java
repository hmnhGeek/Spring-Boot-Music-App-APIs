package com.musicapp.music_app.controllers;

import DTOs.requests.PasswordRequestDTO;
import com.musicapp.music_app.model.Credential;
import com.musicapp.music_app.repositories.CredentialRepository;
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
    private CredentialRepository credentialRepository;

    @PostMapping("/validate")
    public ResponseEntity<String> checkPassword(@RequestBody PasswordRequestDTO passwordRequest) {
        // Decode the base64 password from the request body
        String userEncodedPassword = passwordRequest.getEncodedPassword();
        String decodedUserPassword = EncryptionManagement.decodeBase64(userEncodedPassword);

        // Fetch the stored credential from MongoDB
        Credential credential = credentialRepository.findById("675840c0cafcb721c5532915").orElse(null);

        if (credential == null) {
            return ResponseEntity.status(404).body("Credential not found.");
        }

        // Decode the base64 password stored in MongoDB
        String decodedStoredPassword = EncryptionManagement.decodeBase64(credential.getPassword());

        // Compare the entered decoded password with the decoded stored password
        if (decodedUserPassword.equals(decodedStoredPassword)) {
            return ResponseEntity.ok("Password is correct.");
        } else {
            return ResponseEntity.status(401).body("Incorrect password.");
        }
    }
}
