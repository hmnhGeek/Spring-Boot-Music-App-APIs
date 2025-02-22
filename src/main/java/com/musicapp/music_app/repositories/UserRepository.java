package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<com.musicapp.music_app.model.User, ObjectId> {
    User findByUserName(String userName);
}
