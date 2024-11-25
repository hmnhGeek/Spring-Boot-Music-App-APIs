package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongRepository extends MongoRepository<Song, String> {
}
