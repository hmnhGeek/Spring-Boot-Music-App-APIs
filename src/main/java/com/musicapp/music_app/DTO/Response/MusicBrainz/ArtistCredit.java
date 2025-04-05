package com.musicapp.music_app.DTO.Response.MusicBrainz;

import lombok.Data;

@Data
public class ArtistCredit{
    public String name;
    public Artist artist;
    public String joinphrase;
}
