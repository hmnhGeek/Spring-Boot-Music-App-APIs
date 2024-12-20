package com.musicapp.music_app.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.List;

@Document(collection = "playlists")
public class Playlist {

    @Id
    private String id;
    private String name;
    private List<String> songIds;
    private boolean protectedPlaylist;  // New field

    // Constructors, getters, and setters

    public Playlist(String name, List<String> songIds, boolean protectedPlaylist) {
        this.name = name;
        this.songIds = songIds;
        this.protectedPlaylist = protectedPlaylist;
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

    public boolean isProtectedPlaylist() {
        return protectedPlaylist;
    }

    public void setProtectedPlaylist(boolean protectedPlaylist) {
        this.protectedPlaylist = protectedPlaylist;
    }
}
