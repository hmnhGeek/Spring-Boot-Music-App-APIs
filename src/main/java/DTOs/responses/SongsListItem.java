package DTOs.responses;

import lombok.Data;

@Data
public class SongsListItem {
    private String id;
    private String originalName;
    private String coverImageData;
}
