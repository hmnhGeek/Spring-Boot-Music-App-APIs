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

    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true; // File deleted successfully
            } else {
                System.err.println("File not found: " + filePath);
                return false; // File does not exist
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + filePath + " - " + e.getMessage());
            return false; // Error occurred during file deletion
        }
    }

    public static boolean deleteFiles(String... filePaths) {
        boolean allDeleted = true;
        for (String filePath : filePaths) {
            boolean deleted = deleteFile(filePath);
            if (!deleted) {
                allDeleted = false; // At least one file failed to delete
            }
        }
        return allDeleted; // Return true only if all files were successfully deleted
    }
}
