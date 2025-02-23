package com.musicapp.music_app.services;

import com.musicapp.music_app.DTO.Requests.User.CreateUserRequestDTO;
import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.UserRepository;
import com.musicapp.music_app.utils.EncryptionManagement;
import com.musicapp.music_app.utils.FileManagementUtility;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String PROFILE_FOLDER = "profiles";

    /**
     *
     * @param user Parameter of type {@code User}.
     * @return {@code User} by saving the entry inside the MongoDB collection.
     */
    public User save(CreateUserRequestDTO user, MultipartFile profileImage) throws Exception {
        FileManagementUtility.createFolderIfNotExists(PROFILE_FOLDER);

        List<String> profileImageDetails = FileManagementUtility.getFilenameAndExtension(Objects.requireNonNull(profileImage.getOriginalFilename()));
        SecretKey encryptionKey = EncryptionManagement.generateEncryptionKey();
        String profileImagePath = EncryptionManagement.saveEncryptedFile(profileImage.getInputStream(), PROFILE_FOLDER, encryptionKey);

        User savedUser = new User();
        savedUser.setUserName(user.getUserName());
        savedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        savedUser.setRoles(List.of("USER"));
        savedUser.setProfileImagePath(profileImagePath);
        savedUser.setProfileImageExtension(profileImageDetails.get(1));
        savedUser.setEncryptionKey(Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
        return userRepository.save(savedUser);
    }

    /**
     *
     * @param user A {@code User} class object in which a new journal entry needs addition.
     * @param savedSong A journal entry that needs to be added into the user.
     */
    public void addJournalEntryInUser(User user, Song savedSong) {
        user.getSongs().add(savedSong);
        userRepository.save(user);
    }

    public void removeSong(User user, ObjectId id) {
        user.getSongs().removeIf(x -> x.getId().equals(id));
        userRepository.save(user);
    }

    /**
     * This method returns all the saved users from the collection.
     * @return The return type is {@code List<User>}.
     */
    public List<User> getAll() {
        List<User> allUsers = userRepository.findAll();
        return allUsers;
    }

    /**
     *
     * @param id A parameter of type {@code ObjectId} corresponding to the primary key in the mongodb collection.
     * @return Optional user.
     */
    public Optional<User> getById(ObjectId id) {
        // Optional is like a box; there can be data inside it or not.
        Optional<User> user = userRepository.findById(id);
        return user;
    }

    /**
     *
     * @param id A parameter of type {@code ObjectId} corresponding to the primary key in the mongodb
     */
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }

    /**
     *
     * @param userName A string value denoting the case-sensitive username.
     * @return A {@code User} data type extracted from the database.
     */
    public User findByUserName(String userName) {
        User user = userRepository.findByUserName(userName);
        return user;
    }

    /**
     *
     * @param user A type of {@code User} whose username or password needs to be updated.
     */
//    public void updateUser(User user) {
//        // To authenticate a user from headers use this syntax.
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userName = authentication.getName();
//
//        User savedUser = findByUserName(userName);
//        savedUser.setUserName(user.getUserName());
//        savedUser.setPassword(user.getPassword());
//        save(savedUser);
//    }

    /**
     * Delete the user in session.
     */
    public void deleteUser() {
        // To authenticate a user from headers use this syntax.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = findByUserName(username);
        deleteById(user.getId());
    }

    public HashMap<String, Object> getDecryptedProfileImage() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.findByUserName(authentication.getName());

        // Decode the encryption key
        String encryptionKeyBase64 = user.getEncryptionKey();
        SecretKey encryptionKey = EncryptionManagement.getSecretKeyFromBase64(encryptionKeyBase64);

        // Decrypt the profile image using the encryption key
        byte[] decryptedData = EncryptionManagement.decryptFile(user.getProfileImagePath(), encryptionKey);

        String originalFilename = user.getUserName();
        String originalExtension = user.getProfileImageExtension();
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
