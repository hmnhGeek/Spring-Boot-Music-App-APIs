package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Credential;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CredentialRepository extends MongoRepository<Credential, String> {
}
