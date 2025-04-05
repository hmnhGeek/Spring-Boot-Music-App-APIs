package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Medium{
    public int position;
    public String format;
    public ArrayList<Track> track;
    @JsonProperty("track-count")
    public int trackCount;
    @JsonProperty("track-offset")
    public int trackOffset;
}
