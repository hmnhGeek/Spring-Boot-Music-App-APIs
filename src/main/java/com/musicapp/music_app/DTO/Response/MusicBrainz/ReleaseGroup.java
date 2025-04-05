package com.musicapp.music_app.DTO.Response.MusicBrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ReleaseGroup{
    public String id;
    @JsonProperty("type-id")
    public String typeId;
    @JsonProperty("primary-type-id")
    public String primaryTypeId;
    public String title;
    @JsonProperty("primary-type")
    public String primaryType;
    @JsonProperty("secondary-type")
    public ArrayList<String> secondaryTypes;
    @JsonProperty("secondary-type-ids")
    public ArrayList<String> secondaryTypeIds;
    public String disambiguation;
}
