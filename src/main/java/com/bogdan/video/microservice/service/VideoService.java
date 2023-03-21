package com.bogdan.video.microservice.service;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.dao.PlayListDao;
import com.bogdan.video.microservice.dao.VideoDao;
import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.view.PlayList;
import com.bogdan.video.microservice.view.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VideoService {

    private VideoDao videoDao;
    private RestTemplate restTemplate;
    private PlayListDao playListDao;

    public VideoService(VideoDao videoDao, RestTemplate restTemplate, PlayListDao playListDao){
        this.videoDao = videoDao;
        this.restTemplate = restTemplate;
        this.playListDao = playListDao;
    }

    public Page<Video> loadVideos(Pageable pageable) {
        return videoDao.findAll(pageable);
    }

    public List<Video> getAllVideos(){
        return videoDao.findAll();
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
            newVideo.setIdUser(getIdFromAccountMicroservice(getEmailFromToken()));
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

        private String getEmailFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.getName());
        return authentication.getName();
    }

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

    public ResponseEntity addVideoToPlaylist(final Long idVideo, final Long idPlayList){
        try {
            playListDao.insertPlayListVideo(idVideo, idPlayList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Nu s-a adaugat");
        }
        return ResponseEntity.ok("S-a adaugat cu succes");
    }

    public ResponseEntity removeVideoFromPlaylist(final Long idVideo, final Long idPlayList) {
        try {
            playListDao.deletePlayListVideo(idVideo, idPlayList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Nu s-a sters");
        }
        return ResponseEntity.ok("Videoclipul a fost sters din playlist");
    }

    public List<Video> getVideosFromPlayList(final Long idPlayList) {
        final PlayList playList = playListDao.findById(idPlayList).get();
        final List<Video> videos = new ArrayList<>();
        videos.addAll(playList.getVideos());
        return videos;
    }

    public String findVideoTitleByVideoId(final Long id){
        final Video video = videoDao.findVideoTitleById(id);
        return video.getTitle();
    }

}
