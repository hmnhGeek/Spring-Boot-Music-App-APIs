package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
import DTOs.responses.SongsListItem;
import com.musicapp.music_app.model.Playlist;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.PlaylistRepository;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.repositories.UserRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private CredentialService credentialService;

    private static final String PLAYLISTS_COVER_IMAGE_FOLDER = "playlist_covers";

    public Playlist createPlaylist(AddPlaylistRequestDTO addPlaylistRequestDTO, MultipartFile coverImage) throws Exception {
        FileManagementUtility.createFolderIfNotExists(PLAYLISTS_COVER_IMAGE_FOLDER);

        List<String> coverImageDetails = FileManagementUtility.getFilenameAndExtension(Objects.requireNonNull(coverImage.getOriginalFilename()));
        SecretKey encryptionKey = EncryptionManagement.generateEncryptionKey();
        String coverImagePath = EncryptionManagement.saveEncryptedFile(coverImage.getInputStream(), PLAYLISTS_COVER_IMAGE_FOLDER, encryptionKey);

        Playlist savedPlaylist = new Playlist();
        savedPlaylist.setTitle(addPlaylistRequestDTO.getTitle());
        savedPlaylist.setDescription(addPlaylistRequestDTO.getDescription());
        savedPlaylist.setCoverImagePath(coverImagePath);
        savedPlaylist.setCoverImageName(coverImageDetails.get(0));
        savedPlaylist.setCoverImageExtension(coverImageDetails.get(1));
        savedPlaylist.setEncryptionKey(Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
        Playlist finalSavedPlaylist = playlistRepository.save(savedPlaylist);

        // adding the playlist into user's collection.
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(userName);
        user.getPlaylists().add(finalSavedPlaylist);
        userRepository.save(user);

        return finalSavedPlaylist;
    }

    public void addSongToPlaylist(String songId, String playlistId) {
        Optional<Song> song = songRepository.findById(songId);
        if (song.isEmpty()) {
            return;
        }
        Song mainSong = song.get();
        Optional<Playlist> playlist = playlistRepository.findById(new ObjectId(playlistId));
        if (playlist.isEmpty()) {
            return;
        }
        Playlist mainPlaylist = playlist.get();
        mainPlaylist.getSongs().add(mainSong);
        playlistRepository.save(mainPlaylist);
    }

    public void removeSongFromPlaylist(String playlistId, String songId) {
        Optional<Playlist> playlist = playlistRepository.findById(new ObjectId(playlistId));
        if (playlist.isEmpty()) {
            return;
        }
        Playlist mainPlaylist = playlist.get();
        mainPlaylist.getSongs().removeIf(x -> x.getId().equals(songId));
        playlistRepository.save(mainPlaylist);
    }

    public List<Playlist> getAllPlaylists() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username);
        return user.getPlaylists();
    }

    public void removePlaylist(String playlistId) {
        Optional<Playlist> playlistBox = playlistRepository.findById(new ObjectId(playlistId));
        if (playlistBox.isEmpty()) {
            return;
        }
        Playlist playlist = playlistBox.get();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username);
        user.getPlaylists().removeIf(x -> x.getId().equals(playlist.getId()));

        playlistRepository.deleteById(playlist.getId());
        FileManagementUtility.deleteFiles(playlist.getCoverImagePath());

        userRepository.save(user);
    }

    public List<SongsListItem> getSongs(String playlistId) {
        ObjectId id = new ObjectId(playlistId);
        Optional<Playlist> box = playlistRepository.findById(id);
        if (box.isEmpty()) {
            return null;
        }
        Playlist playlist = box.get();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username);
        if (user.getPlaylists().stream().filter(x -> x.getId().equals(playlist.getId())).toList().isEmpty()) {
            return null;
        }
        return playlist.getSongs().stream().map(x -> {
            SongsListItem song = new SongsListItem();
            song.setId(x.getId());
            song.setOriginalName(x.getOriginalName());
            return song;
        }).toList();
    }

    public HashMap<String, Object> getDecryptedCoverById(String playlistId) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByUserName(authentication.getName());
        List<Playlist> playlists = user.getPlaylists().stream().filter(x -> x.getId().equals(new ObjectId(playlistId))).toList();

        if (playlists.isEmpty()) {
            return null; // Return null if the song is not found
        }

        Playlist playlist = playlists.get(0);

        // Decode the encryption key
        String encryptionKeyBase64 = playlist.getEncryptionKey();
        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(encryptionKeyBase64);

        // Decrypt the song file using the encryption key
        byte[] decryptedData = EncryptionManagement.decryptFile(playlist.getCoverImagePath(), encryptionKey);

        String originalFilename = playlist.getCoverImageName();
        String originalExtension = playlist.getCoverImageExtension();
        String decryptedFilename = originalFilename + "." + originalExtension;

        // Prepare the ByteArrayResource to send back the decrypted file as a blob
        ByteArrayResource resource = new ByteArrayResource(decryptedData);

        // Return the decrypted file as a response entity with the appropriate headers
        HashMap<String, Object> map = new HashMap<>();
        map.put("filename", decryptedFilename);
        map.put("file", resource);
        return map;
    }
}
