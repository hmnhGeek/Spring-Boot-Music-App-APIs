package com.musicapp.music_app.repositories;

import com.musicapp.music_app.model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomSongRepositoryImpl implements CustomSongRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateVaultProtectedFlag(String id, boolean vaultProtected) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("vault_protected", vaultProtected);

        mongoTemplate.updateFirst(query, update, Song.class);
    }
}
