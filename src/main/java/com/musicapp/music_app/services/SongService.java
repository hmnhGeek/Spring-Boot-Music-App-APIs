package com.musicapp.music_app.services;

import DTOs.requests.SongUploadRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.repositories.CustomSongRepositoryImpl;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    @Autowired
    private CustomSongRepositoryImpl customSongRepositoryImpl;

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

    public HashMap<String, Object> getDecryptedSongCoverById(String songId) throws Exception {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return null; // Return null if the song is not found
        }

        Song song = optionalSong.get();

        // Decode the encryption key
        String encryptionKeyBase64 = song.getEncryptionKey();
        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(encryptionKeyBase64);

        // Decrypt the song file using the encryption key
        byte[] decryptedData = EncryptionManagement.decryptFile(song.getCoverImagePath(), encryptionKey);

        String originalFilename = song.getOriginalCoverImageName();
        String originalExtension = song.getCoverImageExtension();
        String decryptedFilename = originalFilename + "." + originalExtension;

        // Prepare the ByteArrayResource to send back the decrypted file as a blob
        ByteArrayResource resource = new ByteArrayResource(decryptedData);

        // Return the decrypted file as a response entity with the appropriate headers
        HashMap<String, Object> map = new HashMap<>();
        map.put("filename", decryptedFilename);
        map.put("file", resource);
        return map;
    }

    public HashMap<String, Object> getDecryptedSongById(String songId) throws Exception {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return null; // Return null if the song is not found
        }

        Song song = optionalSong.get();

        // Decode the encryption key
        String encryptionKeyBase64 = song.getEncryptionKey();
        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(encryptionKeyBase64);

        // Decrypt the song file using the encryption key
        byte[] decryptedData = EncryptionManagement.decryptFile(song.getFilePath(), encryptionKey);

        String originalFilename = song.getOriginalName();
        String originalExtension = song.getFileExtension();
        String decryptedFilename = originalFilename + "." + originalExtension;

        // Prepare the ByteArrayResource to send back the decrypted file as a blob
        ByteArrayResource resource = new ByteArrayResource(decryptedData);

        // Return the decrypted file as a response entity with the appropriate headers
        HashMap<String, Object> map = new HashMap<>();
        map.put("filename", decryptedFilename);
        map.put("file", resource);
        return map;
    }

    public List<SongsListItem> getSongsList(boolean vaultProtected) {
        List<Song> songs;
        if(!vaultProtected) {
            songs = songRepository.findAllNonProtectedSongs();
        }
        else {
            songs = songRepository.findAllProtectedSongs();
        }
        List<SongsListItem> songsListItemList = songs.stream().map(song -> {
            SongsListItem songsListItem = new SongsListItem();
            songsListItem.setId(song.getId());
            songsListItem.setOriginalName(song.getOriginalName());

            try {
                // Get the decrypted cover image for each song (without modifying getDecryptedSongCoverById logic)
                HashMap<String, Object> coverImageData = getDecryptedSongCoverById(song.getId());
                if (coverImageData != null) {
                    // Retrieve the decrypted cover image as a byte array
                    ByteArrayResource coverImageResource = (ByteArrayResource) coverImageData.get("file");
                    byte[] coverImageBytes = coverImageResource.getByteArray();

                    // Convert byte array to Base64 string
                    String base64CoverImage = Base64.getEncoder().encodeToString(coverImageBytes);
                    songsListItem.setCoverImageData(base64CoverImage);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle the error if necessary
            }

            return songsListItem;
        }).collect(Collectors.toList());

        return songsListItemList;
    }


    public void updateVaultProtected(String id, boolean vaultProtected) {
        customSongRepositoryImpl.updateVaultProtectedFlag(id, vaultProtected);
    }

}
