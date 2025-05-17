package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Alias{
    @JsonProperty("sort-name")
    public String sortName;
    public String name;
    public String locale;
    public String type;
    public boolean primary;
    @JsonProperty("begin-date")
    public Object beginDate;
    @JsonProperty("end-date")
    public Object endDate;
    @JsonProperty("type-id")
    public String typeId;
}
