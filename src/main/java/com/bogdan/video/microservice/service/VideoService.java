package com.bogdan.video.microservice.service;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.dao.VideoDao;
import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.view.Video;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoService {

    private VideoDao videoDao;
    private RestTemplate restTemplate;

    public VideoService(VideoDao videoDao, RestTemplate restTemplate){
        this.videoDao = videoDao;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> uploadVideo(String title, String description, MultipartFile inputFile) {
        final String EXTENSION = ".mp4";
        try {
            if (!"video/mp4".equals(inputFile.getContentType())) {
                throw new VideoException("Format invalid!");
            }
            Video newVideo = new Video();
            newVideo.setTitle(title);
            newVideo.setDescription(description);
            newVideo.setIdUser(getIdFromAccountMicroservice("nedelcubogdanvalentin@gmail.com"));
            newVideo = videoDao.save(newVideo);


            byte[] bytes = inputFile.getBytes();
            Path path = Paths.get(AppConstants.STORAGE_PATH + newVideo.getId() + EXTENSION);
            Files.write(path, bytes);

            return ResponseEntity.ok().body("Upload reusit");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Upload nereusit");
        }
    }


//    private String getIdFromAccountMicroservice(final String email) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//            HttpEntity<String> entity = new HttpEntity<String>(headers);
//            return restTemplate.exchange( "http://localhost:8080/videoplatform/api/account/getIdByEmail", HttpMethod.POST, entity, String.class).getBody();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    private Integer getIdFromAccountMicroservice(final String email) {
        ResponseEntity<Integer> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create a JSON payload
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            // Send the POST request with the JSON payload
            response = restTemplate.postForEntity("http://localhost:8080/videoplatform/api/account/getIdByEmail", request, Integer.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

}
