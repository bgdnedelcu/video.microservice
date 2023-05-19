package com.bogdan.video.microservice.controller;

import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.service.VideoService;
import com.bogdan.video.microservice.view.Video;
import com.bogdan.video.microservice.view.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("videoplatform/api/video")
public class VideoContoller {

    private final VideoService videoService;

    public VideoContoller(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("home")
    public ResponseEntity<List<VideoForHomeDto>> loadVideos(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        List<VideoForHomeDto> videos = videoService.loadVideos(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @GetMapping(value = "play/{videoUrl}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<FileSystemResource> playVideo(@PathVariable("videoUrl") final String videoUrl) {
        return videoService.playVideo(videoUrl);
    }

    @GetMapping("search/{text}")
    public ResponseEntity<List<VideoForHomeDto>> search(@PathVariable("text") final String text) {
        List<VideoForHomeDto> videos = videoService.loadVideosForSearch(text);
        return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @GetMapping("playList/{idplayList}")
    public List<VideoForHomeDto> getVideosFromPlayList(@PathVariable("idplayList") final Long idPlayList) {
        return videoService.getVideosFromPlayList(idPlayList);
    }

    @GetMapping("videoTitle/{videoId}")
    public String findVideoTitleByVideoId(@PathVariable("videoId") final Long videoId) {
        return videoService.findVideoTitleByVideoId(videoId);
    }

    @GetMapping("videoById/{id}")
    public Video getVideoById(@PathVariable("id") final Long id) throws VideoException {
        return videoService.getVideoById(id);
    }

    @GetMapping("commentsByVideoId/{videoId}")
    public List<VideoCommentDto> getCommentsByVideoId(@PathVariable("videoId") final Long id) {
        return videoService.getCommentsByVideoId(id);
    }

    @GetMapping("getVideoDetails/{videoId}")
    public VideoDetailsDto getVideoDetails(@PathVariable("videoId") final Long id) throws VideoException {
        return videoService.getVideoDetails(id);
    }

    @GetMapping("videoDetailsForNonUsers/{videoId}")
    public VideoDetailsForNonUsers getVideoDetailsForNonUsers(@PathVariable("videoId") final Long id) throws VideoException {
        return videoService.getVideoDetailsForNonUsers(id);
    }

    @GetMapping("getVideosByChannelName/{channelName}")
    public List<VideoForHomeDto> getVideosByChannelName(@PathVariable("channelName") final String channelName) {
        return videoService.getVideoByChannelName(channelName);
    }

    @GetMapping("videosForChannel/{channelName}")
    public List<VideoForChannelDto> getVideosForChannel(@PathVariable("channelName") final String channelName) {
        return videoService.getVideosForChannel(channelName);
    }

    @GetMapping("getLogUserId")
    public ResponseEntity getLogUserId() {
        return videoService.getLogUserId();
    }

    @GetMapping("checkVideoId/{videoId}")
    public ResponseEntity checkVideoId(@PathVariable("videoId") final Long videoId) {
        return videoService.checkVideoId(videoId);
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("title") String title,
                                         @RequestParam("description") String description, @RequestParam("file") MultipartFile uploadVideo) {
        return videoService.uploadVideo(title, description, uploadVideo);
    }

    @PostMapping("insertToPlaylist")
    public ResponseEntity insertToPlaylist(@RequestBody final PlayListVideo playListVideo) {
        int id = videoService.addVideoToPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
        return ResponseEntity.ok(id);
    }

    @PostMapping("addComment")
    public void addComment(@RequestBody String content, @RequestParam("idVideo") Long idVideo) {
        videoService.addComment(content, idVideo);
    }

    @PostMapping("like/{videoId}")
    public ResponseEntity likeVideo(@PathVariable("videoId") Long videoId) {
        return videoService.likeVideo(videoId);
    }

    @PostMapping("deleteLike/{videoId}")
    public ResponseEntity deleteLike(@PathVariable("videoId") final Long videoId) {
        return videoService.deleteLike(videoId);
    }

    @PostMapping("deleteVideoFromPlaylist")
    public ResponseEntity<String> removeVideoFromPlayList(@RequestBody final PlayListVideo playListVideo) {
        int noOfDeletedRows = videoService.removeVideoFromPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
        return ResponseEntity.status(HttpStatus.OK).body(String.format("deleted %s rows", noOfDeletedRows));
    }

    @PostMapping("deleteAllVideosFromPlaylist")
    public ResponseEntity<String> deleteAllVideosFromPlaylist(@RequestBody final PlayListVideo playListVideo) {
        int noOfDeletedRows = videoService.deleteAllVideosFromPlaylist(playListVideo.getIdPlayList());
        return ResponseEntity.status(HttpStatus.OK).body(String.format("deleted %s rows", noOfDeletedRows));
    }

    @DeleteMapping("deleteCommentById/{id}")
    public ResponseEntity deleteCommentById(@PathVariable("id") final Long commentId) {
        return videoService.deleteCommentById(commentId);
    }

    @DeleteMapping("deleteVideoById/{id}")
    public ResponseEntity deleteVideoById(@PathVariable("id") final Long videoId) {
        return videoService.deleteVideoById(videoId);
    }

}
