package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {
    @Query("{ 'vault_protected': false }")
    public List<Song> findAllNonProtectedSongs();
}
