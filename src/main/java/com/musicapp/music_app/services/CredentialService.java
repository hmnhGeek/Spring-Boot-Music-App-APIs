package com.musicapp.music_app.services;

import DTOs.requests.PasswordRequestDTO;
import com.musicapp.music_app.model.Credential;
import com.musicapp.music_app.repositories.CredentialRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    public boolean isValidPassword(PasswordRequestDTO passwordRequest) {
        // Decode the base64 password from the request body
        String userEncodedPassword = passwordRequest.getEncodedPassword();
        String decodedUserPassword = EncryptionManagement.decodeBase64(userEncodedPassword);

        // Fetch the stored credential from MongoDB
        Credential credential = credentialRepository.findAll().get(0);

        if (credential == null) {
            return false;
        }

        // Decode the base64 password stored in MongoDB
        String decodedStoredPassword = EncryptionManagement.decodeBase64(credential.getPassword());

        // Compare the entered decoded password with the decoded stored password
        if (decodedUserPassword.equals(decodedStoredPassword)) {
            return true;
        } else {
            return false;
        }
    }
}
