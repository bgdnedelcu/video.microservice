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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoService {

    private final VideoDao videoDao;
    private final PlayListDao playListDao;
    private final CommentDao commentDao;
    private final LikeDao likeDao;
    @Autowired
    private final UtilityService utilityService;

    public List<VideoForHomeDto> loadVideos(Pageable pageable) {
        List<Video> videosPage = videoDao.findAll(pageable).getContent();

        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();
        for (Video video : videosPage) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(utilityService.getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
    }

    public List<VideoForHomeDto> getVideosFromPlayList(final Long idPlayList) {
        final PlayList playList = playListDao.findById(idPlayList).get();

        final List<Video> videosPage = playList.getVideos();

        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();
        for (Video video : videosPage) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(utilityService.getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
    }

    public List<VideoForHomeDto> getVideoByChannelName(final String channelName) {

        final List<Video> videos = videoDao.findAllByIdUser(utilityService.getIdByChannelName(channelName));

        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();
        for (Video video : videos) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(utilityService.getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
    }

    public List<VideoForHomeDto> loadVideosForSearch(String searchText) {
        List<Video> videosPage = videoDao.findByTitleOrDescription(searchText);
        List<VideoForHomeDto> videoForHomeDtos = new ArrayList<>();

        for (Video video : videosPage) {
            VideoForHomeDto videoForHomeDto = new VideoForHomeDto();
            videoForHomeDto.setVideoId(video.getId());
            videoForHomeDto.setVideoTitle(video.getTitle());
            videoForHomeDto.setVideoChannel(utilityService.getChannelNameByUserId((long) video.getIdUser()));
            videoForHomeDtos.add(videoForHomeDto);
        }
        return videoForHomeDtos;
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
            newVideo.setIdUser(utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken()));
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

    public int addVideoToPlaylist(final Long idVideo, final Long idPlayList) {

        return playListDao.insertPlayListVideo(idVideo, idPlayList);
    }

    public int removeVideoFromPlaylist(final Long idVideo, final Long idPlayList) {

        return playListDao.deleteVideoFromPlaylist(idVideo, idPlayList);
    }


    public int deleteAllVideosFromPlaylist(final Long idPlayList){
        return playListDao.deleteAllVideosFromPlaylist(idPlayList);
    }

    public String findVideoTitleByVideoId(final Long id) {
        final Video video = videoDao.findVideoTitleById(id);
        return video.getTitle();
    }

    public ResponseEntity addComment(final String content, final Long videoId) {

        final Comment comment = new Comment();
        comment.setIdUser(utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken()));
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
        Integer currentId = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());
        final VideoLikes videoLikes = new VideoLikes();
        final Video video = new Video();
        video.setId(videoId);
        videoLikes.setVideo(video);
        videoLikes.setIdUser(currentId);
        videoLikes.setLiked(1);

        likeDao.save(videoLikes);

        return ResponseEntity.ok().body("Like-ul a fost inregistrat");

    }

    public ResponseEntity deleteLike(final Long videoId) {
        Integer currentId = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());

        likeDao.deleteLike(videoId, Long.valueOf(currentId));

        return ResponseEntity.ok().body("Like-ul a fost actualizat");

    }

    public Video getVideoById(Long id) {
        Optional<Video> video = videoDao.findById(id);

        if (video.isEmpty()) {
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
            String channelName = utilityService.getChannelNameByUserId((long) item.getIdUser());
            videoCommentDto.setChannelName(channelName);
            videoCommentDto.setComment(item.getContent());
            videoCommentDtos.add(videoCommentDto);
        });
        return videoCommentDtos;
    }

    public VideoDetailsDto getVideoDetails(Long videoId) throws VideoException {
        final Optional<Video> videoOptional = videoDao.findById(videoId);
        VideoDetailsDto videoDetailsDto = new VideoDetailsDto();
        if (videoOptional.isEmpty()) {
            throw new VideoException("Not found");
        }

        videoDetailsDto.setVideoTitle(videoOptional.get().getTitle());
        videoDetailsDto.setDescription(videoOptional.get().getDescription());
        videoDetailsDto.setLikes(videoOptional.get().getVideoLikesList().size());
        Integer currentiD = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());
        for (VideoLikes videoLikes : videoOptional.get().getVideoLikesList()) {
            if (videoLikes.getIdUser() == currentiD) {
                videoDetailsDto.setLiked(true);
            }
        }
        String channelName = utilityService.getChannelNameByUserId((long) videoOptional.get().getIdUser());
        videoDetailsDto.setVideoChannelName(channelName);
        return videoDetailsDto;
    }

    public ResponseEntity getLogUserId() {
       long id = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());
       return ResponseEntity.ok().body(id);
    }

    public ResponseEntity deleteCommentById(Long commentId) {
        commentDao.deleteById(commentId);
        return ResponseEntity.ok().body("Comment has been deleted");
    }

}

