package com.musicapp.music_app.services;

import DTOs.requests.SongUploadRequestDTO;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.util.Base64;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    private static final String MUSIC_FOLDER = "music";
    private static final String COVERS_FOLDER = "covers";


    public Song uploadAndEncryptSong(InputStream musicFile, InputStream coverImage) throws Exception {
        FileManagementUtility.createFolderIfNotExists(MUSIC_FOLDER);
        FileManagementUtility.createFolderIfNotExists(COVERS_FOLDER);
        SecretKey encryptionKey = EncryptionManagement.generateEncryptionKey();
        String musicFilePath = EncryptionManagement.saveEncryptedFile(musicFile, MUSIC_FOLDER, encryptionKey);
        String coverImagePath = EncryptionManagement.saveEncryptedFile(coverImage, COVERS_FOLDER, encryptionKey);
        Song song = new Song();
        song.setFilePath(musicFilePath);
        song.setCoverImagePath(coverImagePath);
        song.setEncryptionKey(Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
        return songRepository.save(song);
    }



}
