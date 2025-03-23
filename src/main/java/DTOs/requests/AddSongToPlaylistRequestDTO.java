package DTOs.requests;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
public class AddSongToPlaylistRequestDTO {
    private List<String> songIds;
}
