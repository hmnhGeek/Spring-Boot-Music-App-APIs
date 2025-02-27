package com.musicapp.music_app.DTO.Requests.User;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String userName;
    private String password;
}
