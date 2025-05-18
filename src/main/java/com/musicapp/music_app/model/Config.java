package com.musicapp.music_app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    @Id
    private ObjectId id;
    private String key;
    private String value;
}
