package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Artist{
    public String id;
    public String name;
    @JsonProperty("sort-name")
    public String sortName;
    public ArrayList<Alias> aliases;
    public String disambiguation;
}
