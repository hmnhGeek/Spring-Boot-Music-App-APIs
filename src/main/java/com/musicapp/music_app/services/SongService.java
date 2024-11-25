package com.musicapp.music_app.services;

import DTOs.requests.SongUploadRequestDTO;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    private static final String MUSIC_FOLDER = "music";
    private static final String COVERS_FOLDER = "covers";


    public Song uploadAndEncryptSong(MultipartFile musicFile, MultipartFile coverImage) throws Exception {
        FileManagementUtility.createFolderIfNotExists(MUSIC_FOLDER);
        FileManagementUtility.createFolderIfNotExists(COVERS_FOLDER);

        List<String> musicFilenameDetails = FileManagementUtility.getFilenameAndExtension(Objects.requireNonNull(musicFile.getOriginalFilename()));
        List<String> coverFilenameDetails = FileManagementUtility.getFilenameAndExtension(Objects.requireNonNull(coverImage.getOriginalFilename()));

        SecretKey encryptionKey = EncryptionManagement.generateEncryptionKey();
        String musicFilePath = EncryptionManagement.saveEncryptedFile(musicFile.getInputStream(), MUSIC_FOLDER, encryptionKey);
        String coverImagePath = EncryptionManagement.saveEncryptedFile(coverImage.getInputStream(), COVERS_FOLDER, encryptionKey);
        Song song = new Song();
        song.setFilePath(musicFilePath);
        song.setCoverImagePath(coverImagePath);
        song.setOriginalName(musicFilenameDetails.get(0));
        song.setOriginalCoverImageName(coverFilenameDetails.get(0));
        song.setFileExtension(musicFilenameDetails.get(1));
        song.setCoverImageExtension(coverFilenameDetails.get(1));
        song.setEncryptionKey(Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
        return songRepository.save(song);
    }

    public byte[] getDecryptedSongById(String songId) throws Exception {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return null; // Return null if the song is not found
        }

        Song song = optionalSong.get();

        // Decode the encryption key
        String encryptionKeyBase64 = song.getEncryptionKey();
        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(encryptionKeyBase64);

        // Decrypt the song file using the encryption key
        return EncryptionManagement.decryptFile(song.getFilePath(), encryptionKey);
    }

}
