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
import com.bogdan.video.microservice.view.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public List<VideoForHomeDto> loadVideos(final Pageable pageable) {
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
        int id = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());
        if (playList.getIdUser() != id) {
            throw new VideoException("Invalid id");
        }

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

    public List<VideoForChannelDto> getVideosForChannel(final String channelName){
        final List<Video> videos = videoDao.findAllByIdUser(utilityService.getIdByChannelName(channelName));
        List<VideoForChannelDto> videosForChannel = new ArrayList<>();
        for(Video video : videos){
            VideoForChannelDto videoForChannelDto = new VideoForChannelDto();
            videoForChannelDto.setVideoId(video.getId());
            videoForChannelDto.setVideoTitle(video.getTitle());
            videoForChannelDto.setUserId((long) video.getIdUser());
            videosForChannel.add(videoForChannelDto);
        }
        return videosForChannel;
    }

    public List<VideoForHomeDto> loadVideosForSearch(final String searchText) {
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

    public ResponseEntity<String> uploadVideo(final String title, final String description, MultipartFile inputFile) {
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

        final int noOfRecords = playListDao.checkIfExistsRecords(idVideo, idPlayList);
        if (noOfRecords > 0) {
            throw new VideoException(String.format("Video with id %d already exists in playlist with id %d", idVideo, idPlayList));
        }

        return playListDao.insertPlayListVideo(idVideo, idPlayList);
    }

    public int removeVideoFromPlaylist(final Long idVideo, final Long idPlayList) {

        return playListDao.deleteVideoFromPlaylist(idVideo, idPlayList);
    }

    public int deleteAllVideosFromPlaylist(final Long idPlayList) {
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

    public Video getVideoById(final Long id) {
        Optional<Video> video = videoDao.findById(id);

        if (video.isEmpty()) {
            throw new VideoException("Nu am gasit video-ul");
        }
        return video.get();

    }

    public List<VideoCommentDto> getCommentsByVideoId(final Long videoId) {
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

    public VideoDetailsDto getVideoDetails(final Long videoId) throws VideoException {
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

    public VideoDetailsForNonUsers getVideoDetailsForNonUsers(final Long videoId) throws VideoException{
        final Optional<Video> videoOptional = videoDao.findById(videoId);
        VideoDetailsForNonUsers videoDetailsForNonUsers = new VideoDetailsForNonUsers();
        if(videoOptional.isEmpty()){
            throw new VideoException("Not found");
        }
        videoDetailsForNonUsers.setVideoTitle(videoOptional.get().getTitle());
        videoDetailsForNonUsers.setDescription(videoOptional.get().getDescription());
        String channelName = utilityService.getChannelNameByUserId((long) videoOptional.get().getIdUser());
        videoDetailsForNonUsers.setVideoChannelName(channelName);
        return videoDetailsForNonUsers;
    }

    public ResponseEntity getLogUserId() {
        long id = utilityService.getIdFromAccountMicroservice(utilityService.getEmailFromToken());
        return ResponseEntity.ok().body(id);
    }

    public ResponseEntity deleteCommentById(final Long commentId) {
        commentDao.deleteById(commentId);
        return ResponseEntity.ok().body("Comment has been deleted");
    }

    public ResponseEntity deleteVideoById(final Long videoId) {
        Video videoToDelete = videoDao.findById(videoId).orElse(null);
        if (videoToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video not found");
        }

        List<PlayList> playlists = playListDao.findByVideosId(videoId);
        for (PlayList playlist : playlists) {
            playlist.getVideos().removeIf(video -> video.getId().equals(videoId));
            playListDao.save(playlist);
        }

        videoDao.deleteById(videoId);

        final String EXTENSION = ".mp4";
        File fileToDelete = new File(AppConstants.STORAGE_PATH + videoToDelete.getId() + EXTENSION);
        if (fileToDelete.delete()) {
            return ResponseEntity.ok().body("Video has been deleted");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete video");
        }
    }

    public ResponseEntity checkVideoId(final Long videoId){
        Optional<Video> video = videoDao.findById(videoId);
        if(video.isPresent()){
            return ResponseEntity.ok().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }

}

