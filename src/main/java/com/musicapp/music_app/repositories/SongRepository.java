package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {

    @Query("{ 'vault_protected': false }")
    Page<Song> findAllNonProtectedSongs(Pageable pageable);

    @Query("{ 'vault_protected': true }")
    Page<Song> findAllProtectedSongs(Pageable pageable);

    @Query("{ 'vault_protected': false }")
    List<Song> findAllNonProtectedSongsWithoutCover();

    @Query("{ 'vault_protected': true }")
    List<Song> findAllProtectedSongsWithoutCover();

    @Query("{ '_id': { $in: ?0 } }")
    Page<Song> findByIdIn(List<String> songIds, Pageable pageable);
}
