package com.musicapp.music_app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "songs")
@Data
public class Song {
    @Id
    private String id;

    @Field("song_title")
    private String title;

    @Field("song_artist")
    private String artist;

    @Field("album_name")
    private String album;

    @Field("file_path")
    private String filePath;

    @Field("cover_image_path")
    private String coverImagePath;

    @Field("encryption_key")
    private String encryptionKey;

    @Field("file_extension")
    private String fileExtension;

    @Field("cover_image_extension")
    private String coverImageExtension;

    @Field("original_name")
    private String originalName;

    @Field("original_cover_image_name")
    private String originalCoverImageName;

    @Field("vault_protected")
    private boolean vaultProtected;
}
