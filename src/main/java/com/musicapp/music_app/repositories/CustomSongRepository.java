package com.musicapp.music_app.repositories;

public interface CustomSongRepository {
    void updateVaultProtectedFlag(String id, boolean vaultProtected);
}
