package DTOs.responses;

public class PlaylistResponseDTO {

    private String id;
    private String name;
    private boolean protectedPlaylist; // New field for protection status

    // Constructor
    public PlaylistResponseDTO(String id, String name, boolean protectedPlaylist) {
        this.id = id;
        this.name = name;
        this.protectedPlaylist = protectedPlaylist;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isProtectedPlaylist() {
        return protectedPlaylist;
    }

    public void setProtectedPlaylist(boolean protectedPlaylist) {
        this.protectedPlaylist = protectedPlaylist;
    }
}
