package com.musicapp.music_app.services;

import com.musicapp.music_app.DTO.Response.MusicBrainz.RecordingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MusicBrainzService {
    @Autowired
    private RestTemplate restTemplate;

    private static final String url = "https://musicbrainz.org/ws/2/recording/?query=recording:%s&fmt=json";

    public RecordingResponse getMetaDataByTitle(String title) {
        ResponseEntity<RecordingResponse> response = restTemplate.exchange(
                String.format(url, title),
                HttpMethod.GET,
                null,
                RecordingResponse.class
        );
        return response.getBody();
    }
}
