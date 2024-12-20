package com.musicapp.music_app.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.List;

@Document(collection = "playlists")
public class Playlist {

    @Id
    private String id;  // Change ObjectId to String
    private String name;
    private List<String> songIds;  // Change ObjectId to String

    // Constructors, getters, and setters

    public Playlist(String name, List<String> songIds) {
        this.name = name;
        this.songIds = songIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
