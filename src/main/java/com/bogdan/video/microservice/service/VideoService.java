package com.bogdan.video.microservice.service;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.dao.CommentDao;
import com.bogdan.video.microservice.dao.LikeDao;
import com.bogdan.video.microservice.dao.PlayListDao;
import com.bogdan.video.microservice.dao.VideoDao;
import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.view.Comment;
import com.bogdan.video.microservice.view.PlayList;
import com.bogdan.video.microservice.view.Video;
import com.bogdan.video.microservice.view.VideoLikes;
import com.bogdan.video.microservice.view.dto.VideoCommentDto;
import com.bogdan.video.microservice.view.dto.VideoDetailsDto;
import com.bogdan.video.microservice.view.dto.VideoForHomeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class VideoService {

    private final VideoDao videoDao;
    private final RestTemplate restTemplate;
    private final PlayListDao playListDao;
    private final CommentDao commentDao;
    private final LikeDao likeDao;


//    public Page<Video> loadVideos(Pageable pageable) {
//        return videoDao.findAll(pageable);
//    }

    public List<VideoForHomeDto> loadVideos(Pageable pageable) {
        List<Video> videosPage = videoDao.findAll(pageable).getContent();

        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();
        for (Video video : videosPage) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
    }

    public List<VideoForHomeDto> loadVideosForSearch(String searchText){
        List<Video> videosPage = videoDao.findByTitleOrDescription(searchText);
        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();

        for (Video video : videosPage) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
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
        return authentication.getName();
    }

    private Integer getIdFromAccountMicroservice(final String email) {
        ResponseEntity<Integer> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            response = restTemplate.postForEntity("http://localhost:8080/videoplatform/api/account/getIdByEmail", request, Integer.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public int addVideoToPlaylist(final Long idVideo, final Long idPlayList) {

        return playListDao.insertPlayListVideo(idVideo, idPlayList);
    }

    public int removeVideoFromPlaylist(final Long idVideo, final Long idPlayList) {

        return playListDao.deletePlayListVideo(idVideo, idPlayList);

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

        final Comment comment = new Comment();
        comment.setIdUser(getIdFromAccountMicroservice(getEmailFromToken()));
        log.debug(String.valueOf(comment.getIdUser()));
        comment.setContent(content);
        log.debug(content);
        final Video video = new Video();
        video.setId(videoId);
        comment.setVideo(video);
        commentDao.save(comment);

        return ResponseEntity.ok().body("Comentariul a fost adaugat");
    }

    public ResponseEntity likeVideo(final Long videoId) {
        Integer currentId = getIdFromAccountMicroservice(getEmailFromToken());
        final VideoLikes videoLikes = new VideoLikes();
        final Video video = new Video();
        video.setId(videoId);
        videoLikes.setVideo(video);
        videoLikes.setIdUser(currentId);
        videoLikes.setLiked(1);

        likeDao.save(videoLikes);

        return ResponseEntity.ok().body("Like-ul a fost inregistrat");

    }

    public ResponseEntity deletLike(final Long videoId) {
        Integer currentId = getIdFromAccountMicroservice(getEmailFromToken());

        likeDao.deleteLike(videoId, Long.valueOf(currentId));

        return ResponseEntity.ok().body("Like-ul a fost actualizat");

    }

//    public List<Video> search(final String text) {
//        List<Video> videos = new ArrayList<Video>();
//        videos.addAll(videoDao.findByTitleOrDescription(text));
//        return videos;
//    }

    public Video getVideoById(Long id) {
        Optional<Video> video = videoDao.findById(id);

        if (!video.isPresent()) {
            throw new VideoException("Nu am gasit video-ul");
        }
        return video.get();

    }

    public List<VideoCommentDto> getCommentsByVideoId(Long videoId) {
        List<Comment> commentsList = commentDao.findByVideoIdOrderByIdDesc(videoId);
        final List<VideoCommentDto> videoCommentDtos = new ArrayList<>();
        commentsList.forEach((item) -> {
            final VideoCommentDto videoCommentDto = new VideoCommentDto();
            videoCommentDto.setIdComment(item.getId());
            videoCommentDto.setIdUser((long) item.getIdUser());
            String channelName = getChannelNameByUserId((long) item.getIdUser());
            videoCommentDto.setChannelName(channelName);
            videoCommentDto.setComment(item.getContent());
            videoCommentDtos.add(videoCommentDto);
        });
        return videoCommentDtos;
    }

    public String getChannelNameByUserId(Long userId) {
        String url = "http://localhost:8080/videoplatform/api/account/channelNameById/" + userId;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new VideoException("Nu am gasit user-ul");
        }

        return response.getBody();
    }

    public VideoDetailsDto getVideoDetails(Long videoId) throws VideoException {
        final Optional<Video> videoOptional = videoDao.findById(videoId);
        VideoDetailsDto videoDetailsDto = new VideoDetailsDto();
        if (!videoOptional.isPresent()) {
            throw new VideoException("Not found");
        }

        videoDetailsDto.setVideoTitle(videoOptional.get().getTitle());
        videoDetailsDto.setDescription(videoOptional.get().getDescription());
        videoDetailsDto.setLikes(videoOptional.get().getVideoLikesList().size());
        Integer currentiD = getIdFromAccountMicroservice(getEmailFromToken());
        for (VideoLikes videoLikes : videoOptional.get().getVideoLikesList()) {
            if (videoLikes.getIdUser() == currentiD) {
                videoDetailsDto.setLiked(true);
            }
        }
        String channelName = getChannelNameByUserId((long) videoOptional.get().getIdUser());
        videoDetailsDto.setVideoChannelName(channelName);
        return videoDetailsDto;
    }
}

