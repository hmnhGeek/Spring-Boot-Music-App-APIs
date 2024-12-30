package com.musicapp.music_app.services;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DropboxService {
    private static final String ACCESS_TOKEN;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("src/main/resources/config.properties"));
            ACCESS_TOKEN = props.getProperty("dropbox.access.token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load access token", e);
        }
    }

    private final DbxClientV2 client;

    public DropboxService() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/soundly-spring-boot-app").build();
        this.client = new DbxClientV2(config, ACCESS_TOKEN);
    }

    // Upload a file to Dropbox
    public String uploadFile(String localFilePath, String dropboxPath) throws Exception {
        try (InputStream in = new FileInputStream(localFilePath)) {
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(in);
            return metadata.getPathLower(); // Returns the file path in Dropbox
        }
    }

    // List all files in a Dropbox folder
    public List<String> listFilesInFolder(String folderPath) throws Exception {
        List<String> files = new ArrayList<>();
        ListFolderResult result = client.files().listFolder(folderPath);
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                if (metadata instanceof FileMetadata) {
                    files.add(metadata.getPathLower());
                }
            }
            if (result.getHasMore()) {
                result = client.files().listFolderContinue(result.getCursor());
            } else {
                break;
            }
        }
        return files;
    }

    // Delete file from Dropbox
    public void deleteFile(String dropboxPath) throws Exception {
        try {
            // Move the file to trash
            client.files().deleteV2(dropboxPath);
        } catch (Exception e) {
            // Log the error but continue with the process
            System.err.println("Error deleting file from Dropbox: " + e.getMessage());
        }
    }

    // Sync local files with Dropbox
    public void syncFilesWithDropbox(String localMusicFolder, String localCoversFolder) throws Exception {
        // List local files
        File musicDir = new File(localMusicFolder);
        File coversDir = new File(localCoversFolder);
        List<String> localMusicFiles = Arrays.asList(Objects.requireNonNull(musicDir.list()));
        List<String> localCoverFiles = Arrays.asList(Objects.requireNonNull(coversDir.list()));

        // List Dropbox files
        List<String> dropboxMusicFiles = listFilesInFolder("/music");
        List<String> dropboxCoverFiles = listFilesInFolder("/covers");

        // Upload missing music files
        for (String musicFile : localMusicFiles) {
            String localFilePath = Paths.get(localMusicFolder, musicFile).toString();
            String dropboxFilePath = "/music/" + musicFile;
            if (!dropboxMusicFiles.contains(dropboxFilePath)) {
                uploadFile(localFilePath, dropboxFilePath);
                System.out.println("Uploaded: " + localFilePath);
            }
        }

        // Upload missing cover files
        for (String coverFile : localCoverFiles) {
            String localFilePath = Paths.get(localCoversFolder, coverFile).toString();
            String dropboxFilePath = "/covers/" + coverFile;
            if (!dropboxCoverFiles.contains(dropboxFilePath)) {
                uploadFile(localFilePath, dropboxFilePath);
                System.out.println("Uploaded: " + localFilePath);
            }
        }

        // Delete extra music files on Dropbox
        for (String dropboxFile : dropboxMusicFiles) {
            if (!localMusicFiles.contains(dropboxFile.replace("/music/", ""))) {
                deleteFile(dropboxFile);
                System.out.println("Deleted from Dropbox: " + dropboxFile);
            }
        }

        // Delete extra cover files on Dropbox
        for (String dropboxFile : dropboxCoverFiles) {
            if (!localCoverFiles.contains(dropboxFile.replace("/covers/", ""))) {
                deleteFile(dropboxFile);
                System.out.println("Deleted from Dropbox: " + dropboxFile);
            }
        }
    }
}
