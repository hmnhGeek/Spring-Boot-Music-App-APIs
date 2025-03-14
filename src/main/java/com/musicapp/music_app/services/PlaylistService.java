package com.musicapp.music_app.services;

import DTOs.requests.AddPlaylistRequestDTO;
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
}
