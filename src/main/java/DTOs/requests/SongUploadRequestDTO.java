package DTOs.requests;

import lombok.Data;

@Data
public class SongUploadRequestDTO {
    private String title;
    private String artist;
    private String album;
}
