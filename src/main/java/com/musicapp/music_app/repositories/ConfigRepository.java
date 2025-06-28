package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Config;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigRepository extends MongoRepository<Config, ObjectId> {
}
