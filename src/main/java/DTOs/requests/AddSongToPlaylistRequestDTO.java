package DTOs.requests;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class AddSongToPlaylistRequestDTO {
    private String playlistId;
    private String songId;
    private String password;
}
