package com.musicapp.music_app.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "slides")
public class Slide {
    @Id
    private String id;

    @NotNull
    private String url;

    @NotNull
    private String key;

    private List<Song> songs;
}
