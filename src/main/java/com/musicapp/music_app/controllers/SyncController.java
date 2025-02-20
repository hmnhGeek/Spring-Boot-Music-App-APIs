package com.musicapp.music_app.controllers;

import com.musicapp.music_app.services.DropboxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync-backup")
public class SyncController {

    private final DropboxService dropboxService;
    private final String MUSIC_FOLDER = "music";
    private final String COVERS_FOLDER = "covers";

    public SyncController() {
        this.dropboxService = new DropboxService();
    }

    @PostMapping
    public String syncBackup() {
        try {
            dropboxService.syncFilesWithDropbox(MUSIC_FOLDER, COVERS_FOLDER);
            return "Sync completed successfully!";
        } catch (Exception e) {
            return "Error occurred during sync: " + e.getMessage();
        }
    }
}
