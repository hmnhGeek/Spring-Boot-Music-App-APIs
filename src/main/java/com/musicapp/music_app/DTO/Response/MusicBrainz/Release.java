package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Release{
    public String id;
    @JsonProperty("status-id")
    public String status_id;
    public int count;
    public String title;
    public String status;
    @JsonProperty("artist-credit")
    public ArrayList<ArtistCredit> artistCredit;
    @JsonProperty("release-group")
    public ReleaseGroup releaseGroup;
    public String date;
    public String country;
    @JsonProperty("release-events")
    public ArrayList<ReleaseEvent> releaseEvents;
    @JsonProperty("track-count")
    public int trackCount;
    public ArrayList<Medium> media;
}
