package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Playlist;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlaylistRepository extends MongoRepository<Playlist, ObjectId> {

}
