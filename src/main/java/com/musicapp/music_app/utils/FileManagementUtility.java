package com.musicapp.music_app.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileManagementUtility {
    public static void createFolderIfNotExists(String folderName) throws Exception {
        Path folderPath = Paths.get(folderName);
        if(!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

    public static List<String> getFilenameAndExtension(String file) {
        // Get the filename without extension
        String baseName = file.substring(0, file.lastIndexOf('.'));

        // Get the file extension (without the dot)
        String extension = file.substring(file.lastIndexOf('.') + 1);

        return Arrays.asList(baseName, extension);
    }
}
