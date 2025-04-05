package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Recording{
    public String id;
    public int score;
    public String title;
    public int length;
    public Object video;
    @JsonProperty("artist-credit")
    public ArrayList<ArtistCredit> artistCredit;
    @JsonProperty("first-release-date")
    public String firstReleaseDate;
    public ArrayList<Release> releases;
    public ArrayList<String> isrcs;
    public ArrayList<Tag> tags;
    public String disambiguation;
}
