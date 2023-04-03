package com.bogdan.video.microservice.service;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.dao.CommentDao;
import com.bogdan.video.microservice.dao.PlayListDao;
import com.bogdan.video.microservice.dao.VideoDao;
import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.view.Comment;
import com.bogdan.video.microservice.view.PlayList;
import com.bogdan.video.microservice.view.Video;
import com.bogdan.video.microservice.view.dto.VideoCommentDto;
import com.bogdan.video.microservice.view.dto.VideoDetailsDto;
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
import java.util.*;

@Service
@Slf4j
public class VideoService {

    private final VideoDao videoDao;
    private final RestTemplate restTemplate;
    private final PlayListDao playListDao;
    private final CommentDao commentDao;

    public VideoService(VideoDao videoDao, RestTemplate restTemplate, PlayListDao playListDao, CommentDao commentDao) {
        this.videoDao = videoDao;
        this.restTemplate = restTemplate;
        this.playListDao = playListDao;
        this.commentDao = commentDao;
    }

    public Page<Video> loadVideos(Pageable pageable) {
        return videoDao.findAll(pageable);
    }

    public List<Video> getAllVideos() {
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
            newVideo.setLikes(0);
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
//        log.debug(authentication.getName());
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

    public ResponseEntity addVideoToPlaylist(final Long idVideo, final Long idPlayList) {
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

    public String findVideoTitleByVideoId(final Long id) {
        final Video video = videoDao.findVideoTitleById(id);
        return video.getTitle();
    }

    public ResponseEntity addComment(final String content, final Long videoId) {
        try {
            final Comment comment = new Comment();
            comment.setIdUser(getIdFromAccountMicroservice(getEmailFromToken()));
            log.debug(String.valueOf(comment.getIdUser()));
            comment.setContent(content);
            log.debug(content);
            final Video video = new Video();
            video.setId(videoId);
            comment.setVideo(video);
            commentDao.save(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Comentariul nu a fost adaugat");
        }
        return ResponseEntity.ok().body("Comentariul a fost adaugat");
    }

    public ResponseEntity likeVideo(final Long videoId)  {
        final Optional<Video> optionalVideo = videoDao.findById(videoId);

        if (optionalVideo.isPresent()) {
               final Video video = optionalVideo.get();
               video.setLikes(video.getLikes() + 1);
               videoDao.save(video);
        }else {
            return ResponseEntity.badRequest().body("Nu s-a inregistrat like-ul");
        }
        return ResponseEntity.ok().body("Like-ul a fost inregistrat");

    }

    public List<Video> search(final String text) {
        List<Video> videos = new ArrayList<Video>();
        videos.addAll(videoDao.findByTitleOrDescription(text));
        return videos;
    }

    public Video getVideoById(Long id) throws VideoException {
        Optional<Video> video = videoDao.findById(id);

        if (!video.isPresent()) {
            throw new VideoException("Nu am gasit video-ul");
        }
        return video.get();

    }
    public List<VideoCommentDto> getCommentsByVideoId(Long videoId) {
        List<Comment> commentsList = commentDao.findByVideoId(videoId);
        final List<VideoCommentDto> videoCommentDtos = new ArrayList<>();
        commentsList.forEach((item) -> {
        final VideoCommentDto videoCommentDto = new VideoCommentDto();
        videoCommentDto.setIdComment(item.getId());
        videoCommentDto.setIdUser((long) item.getIdUser());
            String channelName = null;
            try {
                channelName = getChannelNameByUserId((long) item.getIdUser());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            videoCommentDto.setChannelName(channelName);

        videoCommentDto.setComment(item.getContent());
        videoCommentDtos.add(videoCommentDto);
        });
        return videoCommentDtos;
    }
    public String getChannelNameByUserId(Long userId) throws Exception {
        String url = "http://localhost:8080/videoplatform/api/account/channelNameById/" + userId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new Exception("Nu am gasit user-ul");
        }
    }

    public VideoDetailsDto getVideoDetails(Long videoId) throws VideoException {
        final Optional<Video> videoOptional = videoDao.findById(videoId);
        VideoDetailsDto videoDetailsDto = new VideoDetailsDto();
        if(!videoOptional.isPresent()){
            throw new VideoException("Not found");
        }else {
            videoDetailsDto.setVideoTitle(videoOptional.get().getTitle());
            videoDetailsDto.setDescription(videoOptional.get().getDescription());
            videoDetailsDto.setLikes(videoOptional.get().getLikes());
            String channelName = null;
            try {
                channelName = getChannelNameByUserId((long) videoOptional.get().getIdUser());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            videoDetailsDto.setVideoChannelName(channelName);
        }
        return videoDetailsDto;
    }
}

