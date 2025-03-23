package com.musicapp.music_app.DTO.Response.Playlists;

import lombok.Data;

@Data
public class PlaylistResponseDTO {
    private String id;
    private String title;
    private String description;
    private Integer totalSongs;
}
