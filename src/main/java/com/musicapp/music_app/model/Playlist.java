package com.musicapp.music_app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "playlists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    private ObjectId id;

    @DBRef
    private List<Song> songs = new ArrayList<>();

    private String encryptionKey;

    private String coverImagePath;

    private String coverImageExtension;

    private String description;

    private String title;
}
