package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

@Data
public class RecordingResponse{
    public Date created;
    public int count;
    public int offset;
    public ArrayList<Recording> recordings;
}

