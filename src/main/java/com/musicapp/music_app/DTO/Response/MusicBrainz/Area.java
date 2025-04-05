package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Area{
    public String id;
    public String name;
    @JsonProperty("sort-name")
    public String sortName;
    @JsonProperty("iso-3166-1-codes")
    public ArrayList<String> isoCodes;
}
