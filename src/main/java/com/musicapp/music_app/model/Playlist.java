package com.musicapp.music_app.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.List;

@Document(collection = "playlists")
public class Playlist {

    @Id
    private ObjectId id;
    private String name;
    private List<ObjectId> songIds;

    // Constructors, getters and setters

    public Playlist(String name, List<ObjectId> songIds) {
        this.name = name;
        this.songIds = songIds;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ObjectId> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<ObjectId> songIds) {
        this.songIds = songIds;
    }
}
