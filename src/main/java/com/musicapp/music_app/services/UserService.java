package com.musicapp.music_app.services;

import com.musicapp.music_app.model.Song;
import com.musicapp.music_app.model.User;
import com.musicapp.music_app.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     *
     * @param user Parameter of type {@code User}.
     * @return {@code User} by saving the entry inside the MongoDB collection.
     */
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("USER"));
        User savedJournalEntry = userRepository.save(user);
        return user;
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
    public void updateUser(User user) {
        // To authenticate a user from headers use this syntax.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User savedUser = findByUserName(userName);
        savedUser.setUserName(user.getUserName());
        savedUser.setPassword(user.getPassword());
        save(savedUser);
    }

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
}
