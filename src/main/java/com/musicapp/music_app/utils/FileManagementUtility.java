package com.musicapp.music_app.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManagementUtility {
    public static void createFolderIfNotExists(String folderName) throws Exception {
        Path folderPath = Paths.get(folderName);
        if(!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }
}
