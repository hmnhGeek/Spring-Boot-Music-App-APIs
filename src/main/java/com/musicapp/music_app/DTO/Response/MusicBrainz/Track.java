package com.musicapp.music_app.DTO.Response.MusicBrainz;

import lombok.Data;

@Data
public class Track{
    public String id;
    public String number;
    public String title;
    public int length;
}
