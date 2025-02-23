package com.musicapp.music_app.controllers;

import com.musicapp.music_app.DTO.Requests.User.CreateUserRequestDTO;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Ok";
    }

//    @PostMapping(value = "/create-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDTO user, @RequestPart("profileImagePath") MultipartFile profileImagePath) {
//        try {
//            User savedUser = userService.save(user, profileImagePath);
//            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//        }
//        catch (Exception e) {
//            System.out.println(e.getMessage());
//            // Handle exceptions (e.g., encryption, file saving issues)
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping(value = "/create-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestParam("userName") String userName,
                                        @RequestParam("password") String password,
                                        @RequestPart("profileImagePath") MultipartFile profileImagePath) {
        try {
            CreateUserRequestDTO user = new CreateUserRequestDTO();
            user.setUserName(userName);
            user.setPassword(password);
            User savedUser = userService.save(user, profileImagePath);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
