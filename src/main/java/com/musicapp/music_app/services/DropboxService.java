package com.musicapp.music_app.services;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

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

    public String uploadFile(String localFilePath, String dropboxPath) throws Exception {
        try (InputStream in = new FileInputStream(localFilePath)) {
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(in);
            return metadata.getPathLower(); // Returns the file path in Dropbox
        }
    }

    public void downloadFile(String dropboxPath, String localFilePath) throws Exception {
        try (FileOutputStream out = new FileOutputStream(localFilePath)) {
            client.files().downloadBuilder(dropboxPath)
                    .download(out);
        }
    }

    public void deleteFile(String dropboxPath) throws Exception {
        try {
            // Move the file to trash
            client.files().deleteV2(dropboxPath);
        } catch (Exception e) {
            // Log the error but continue with the process
            System.err.println("Error permanently deleting file from Dropbox: " + e.getMessage());
        }
    }
}