package com.musicapp.music_app.services;

import com.musicapp.music_app.model.Slide;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.SongRepository;
import com.musicapp.music_app.repositories.UserRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class SlidesService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    private boolean userHasAccessToSong(String songId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found: " + userName);
        }

        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) return false;
        Song song = optionalSong.get();
        return user.getSongs().contains(song);
    }

    public void addSlidesByUrls(List<String> urls) {
        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(userName);
            if (user == null) {
                throw new RuntimeException("User not found: " + userName);
            }

            List<Slide> newSlides = new ArrayList<>();

            for (String url : urls) {
                SecretKey secretKey = EncryptionManagement.generateEncryptionKey();
                String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                String encryptedUrl = EncryptionManagement.encryptText(url, secretKey);

                Slide slide = new Slide();
                slide.setUrl(encryptedUrl);
                slide.setKey(base64Key);
                newSlides.add(slide);
            }

            mongoTemplate.insertAll(newSlides); // Insert all slides in one batch
            user.getSlides().addAll(newSlides); // Add to user's slide list
            userRepository.save(user);          // Save once

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting slides", e);
        }
    }

    public void deleteSlideById(String slideId) {
        try {
            Slide slide = findById(slideId);
            if (slide == null) {
                throw new RuntimeException("Slide not found with ID: " + slideId);
            }

            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(userName);
            if (user == null) {
                throw new RuntimeException("User not found: " + userName);
            }

            if (user.getSlides() != null) {
                user.getSlides().removeIf(s -> slideId.equals(s.getId()));
            }

            userRepository.save(user);
            mongoTemplate.remove(slide);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting slide with ID: " + slideId, e);
        }
    }

    public Slide findById(String id) {
        return mongoTemplate.findById(id, Slide.class);
    }

    public int addNewSlidesToSong(String songId, List<String> urls) {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            return 0;
        }

        if (!userHasAccessToSong(songId)) {
            return -1;
        }

        Song song = optionalSong.get();
        List<Slide> newSlides = new ArrayList<>();

        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(userName);
            if (user == null) {
                throw new RuntimeException("User not found: " + userName);
            }

            for (String url : urls) {
                SecretKey secretKey = EncryptionManagement.generateEncryptionKey();
                String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                String encryptedUrl = EncryptionManagement.encryptText(url, secretKey);

                Slide slide = new Slide();
                slide.setUrl(encryptedUrl);
                slide.setKey(base64Key);
                slide.setSongs(new ArrayList<>(List.of(song)));

                newSlides.add(slide);
            }

            mongoTemplate.insertAll(newSlides);
            user.getSlides().addAll(newSlides);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error adding new slides to song", e);
        }
        return 1;
    }

    public int addExistingSlidesToSong(String songId, List<String> slideIds) {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) return 0;
        if (!userHasAccessToSong(songId)) {
            return -1;
        }
        Song song = optionalSong.get();
        slideIds.forEach(id -> {
            Slide slide = findById(id);
            if (slide != null) {
                if (slide.getSongs() == null) {
                    slide.setSongs(new ArrayList<>());
                }
                // Avoid adding duplicates
                boolean alreadyPresent = slide.getSongs().stream()
                        .anyMatch(s -> song.getId().equals(s.getId()));

                if (!alreadyPresent) {
                    slide.getSongs().add(song);
                    mongoTemplate.save(slide);
                }
            }
        });
        return 1;
    }

    public void removeSlidesFromSong(String songId, List<String> slideIds) {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) return;
        Song song = optionalSong.get();
        slideIds.forEach(id -> {
            Slide slide = findById(id);
            if (slide != null && slide.getSongs() != null) {
                slide.getSongs().removeIf(s -> songId.equals(s.getId()));
                mongoTemplate.save(slide);
            }
        });
    }

    public List<String> getSlideImagesForSong(String songId) {
        if (!userHasAccessToSong(songId)) return null;
        List<String> result = new ArrayList<>();
        List<Slide> allSlides = mongoTemplate.findAll(Slide.class);
        for (Slide slide : allSlides) {
            List<Song> songs = slide.getSongs();
            if (songs != null) {
                for (Song song : songs) {
                    if (song != null && songId.equals(song.getId())) {
                        try {
                            String decryptedUrl = EncryptionManagement.decryptText(slide.getUrl(), slide.getKey());
                            result.add(decryptedUrl);
                            break; // found the song in this slide, no need to check further
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to decrypt slide URL", e);
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<String> getDecryptedSlideUrlsForCurrentUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found: " + userName);
        }

        List<String> decryptedUrls = new ArrayList<>();
        if (user.getSlides() != null) {
            for (Slide slide : user.getSlides()) {
                try {
                    SecretKey key = EncryptionManagement.getSecretKeyFromBase64(slide.getKey());
                    String decryptedUrl = EncryptionManagement.decryptText(slide.getUrl(), key);
                    decryptedUrls.add(decryptedUrl);
                } catch (Exception e) {
                    System.out.println("Failed to decrypt slide URL for slide ID: " + slide.getId());
                }
            }
        }
        return decryptedUrls;
    }

}
