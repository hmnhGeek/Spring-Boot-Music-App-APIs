package com.musicapp.music_app.services;

import DTOs.requests.PasswordRequestDTO;
import DTOs.requests.SongUploadRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.CustomSongRepositoryImpl;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.repositories.UserRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private CustomSongRepositoryImpl customSongRepositoryImpl;

    @Autowired
    private UserRepository userRepository;

    private static final String MUSIC_FOLDER = "music";
    private static final String COVERS_FOLDER = "covers";

    public boolean unauthorizedAccessToAsset(String songId, String encodedPassword) {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return true;
        }
        Song song = optionalSong.get();
        PasswordRequestDTO passwordRequestDTO = new PasswordRequestDTO();
        passwordRequestDTO.setEncodedPassword(encodedPassword);
        return song.isVaultProtected() && !credentialService.isValidPassword(passwordRequestDTO);
    }

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

    public HashMap<String, Object> changeSongCoverImage(String songId, MultipartFile newCoverImage) throws Exception {
        List<String> newCoverFilenameDetails = FileManagementUtility.getFilenameAndExtension(Objects.requireNonNull(newCoverImage.getOriginalFilename()));
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return null;
        }
        Song song = optionalSong.get();
        HashMap<String, Object> map = getDecryptedSongCoverById(songId);
        String oldCoverImagePath = song.getCoverImagePath();

        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(song.getEncryptionKey());
        String newCoverImagePath = EncryptionManagement.saveEncryptedFile(newCoverImage.getInputStream(), COVERS_FOLDER, encryptionKey);

        song.setOriginalCoverImageName(newCoverFilenameDetails.get(0));
        song.setCoverImageExtension(newCoverFilenameDetails.get(1));
        song.setCoverImagePath(newCoverImagePath);
        songRepository.save(song);

        try {
            // Delete old cover image file
            FileManagementUtility.deleteFiles(oldCoverImagePath);
        } catch (Exception e) {
            //
        }

        // return the map
        return map;
    }

    public HashMap<String, Object> getDecryptedSongCoverById(String songId) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByUserName(authentication.getName());
        List<Song> songs = user.getSongs().stream().filter(x -> x.getId().equals(songId)).toList();

        if (songs.isEmpty()) {
            return null; // Return null if the song is not found
        }

        Song song = songs.get(0);

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

    public Page<SongsListItem> getSongsList(boolean vaultProtected, Pageable pageable) {
        Page<Song> songsPage;

        // Fetch paginated songs based on the vaultProtected flag
        if (!vaultProtected) {
            songsPage = songRepository.findAllNonProtectedSongs(pageable);
        } else {
            songsPage = songRepository.findAllProtectedSongs(pageable);
        }

        // Map the paginated songs to SongsListItem objects
        List<SongsListItem> songsList = songsPage.stream().map(song -> {
            SongsListItem songsListItem = new SongsListItem();
            songsListItem.setId(song.getId());
            songsListItem.setOriginalName(song.getOriginalName());

            try {
                // Get the decrypted cover image for each song
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

        // Wrap the mapped list into a Page object
        return new PageImpl<>(songsList, pageable, songsPage.getTotalElements());
    }

    public List<SongsListItem> getSongsListWithoutCoverImages(boolean vaultProtected) {
        List<Song> songs;

        // Fetch paginated songs based on the vaultProtected flag
        if (!vaultProtected) {
            songs = songRepository.findAllNonProtectedSongsWithoutCover();
        } else {
            songs = songRepository.findAllProtectedSongsWithoutCover();
        }

        // Map the paginated songs to SongsListItem objects
        List<SongsListItem> songsList = songs.stream().map(song -> {
            SongsListItem songsListItem = new SongsListItem();
            songsListItem.setId(song.getId());
            songsListItem.setOriginalName(song.getOriginalName());
            return songsListItem;
        }).collect(Collectors.toList());

        // Wrap the mapped list into a Page object
        return songsList;
    }



    public void updateVaultProtected(String id, boolean vaultProtected) {
        customSongRepositoryImpl.updateVaultProtectedFlag(id, vaultProtected);
    }

    public boolean deleteSongById(String songId) {
        try {
            Optional<Song> optionalSong = songRepository.findById(songId);
            if (optionalSong.isEmpty()) {
                return false; // Song not found
            }

            Song song = optionalSong.get();

            // Delete song and cover image files
            boolean filesDeleted = FileManagementUtility.deleteFiles(song.getFilePath(), song.getCoverImagePath());

            if (!filesDeleted) {
                System.err.println("Some files could not be deleted for song ID: " + songId);
            }

            // Delete the database record
            songRepository.delete(song);

            return filesDeleted; // Return the status of file deletion
        } catch (Exception e) {
            System.err.println("Error deleting song: " + e.getMessage());
            return false; // Deletion failed
        }
    }

}
