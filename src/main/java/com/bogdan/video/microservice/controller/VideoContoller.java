package com.bogdan.video.microservice.controller;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.exception.VideoException;
import com.bogdan.video.microservice.service.VideoService;
import com.bogdan.video.microservice.view.Video;
import com.bogdan.video.microservice.view.dto.PlayListVideo;
import com.bogdan.video.microservice.view.dto.VideoCommentDto;
import com.bogdan.video.microservice.view.dto.VideoDetailsDto;
import com.bogdan.video.microservice.view.dto.VideoForHomeDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("videoplatform/api/video")
public class VideoContoller {

    private final VideoService videoService;

    public VideoContoller(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("home")
    public ResponseEntity<List<VideoForHomeDto>> loadVideos(Pageable pageable) {
        List<VideoForHomeDto> videos = videoService.loadVideos(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @GetMapping("search/{text}")
    public ResponseEntity<List<VideoForHomeDto>> search(@PathVariable("text") final String text) {
        List<VideoForHomeDto> videos = videoService.loadVideosForSearch(text);
        return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("title") String title,
                                         @RequestParam("description") String description, @RequestParam("file") MultipartFile uploadVideo) {
        return videoService.uploadVideo(title, description, uploadVideo);
    }

    @GetMapping(value = "play/{videoUrl}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<FileSystemResource> playVideo(@PathVariable("videoUrl") final String videoUrl)
            throws FileNotFoundException {
        final String EXTENSION = ".mp4";
        return Mono.fromSupplier(() -> new FileSystemResource(AppConstants.STORAGE_PATH + videoUrl + EXTENSION));
    }

    @PostMapping("insertToPlaylist")
    public ResponseEntity insertToPlaylist(@RequestBody final PlayListVideo playListVideo) {
        int id = videoService.addVideoToPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
        return ResponseEntity.ok(id);
    }

    @PostMapping("removeVideoFromPlaylist")
    public ResponseEntity<String> removeVideoFromPlayList(@RequestBody final PlayListVideo playListVideo) {
        int noOfDeletedRows = videoService.removeVideoFromPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
        return ResponseEntity.status(HttpStatus.OK).body(String.format("deleted %s rows", noOfDeletedRows));
    }

    @PostMapping("deleteAllVideosFromPlaylist")
    public ResponseEntity<String> deleteAllVideosFromPlaylist(@RequestBody final PlayListVideo playListVideo) {
        int noOfDeletedRows = videoService.deleteAllVideosFromPlaylist(playListVideo.getIdPlayList());
        return ResponseEntity.status(HttpStatus.OK).body(String.format("deleted %s rows", noOfDeletedRows));
    }

    @GetMapping("playList/{idplayList}")
    public List<VideoForHomeDto> getVideosFromPlayList(@PathVariable("idplayList") final Long idPlayList) {
        return videoService.getVideosFromPlayList(idPlayList);
    }

    @GetMapping("videoTitle/{videoId}")
    public String findVideoTitleByVideoId(@PathVariable("videoId") final Long videoId) {
        return videoService.findVideoTitleByVideoId(videoId);
    }

    @PostMapping("addComment")
    public void addComment(@RequestBody String content, @RequestParam("idVideo") Long idVideo) {
        videoService.addComment(content, idVideo);
    }

    @PostMapping("like/{videoId}")
    public ResponseEntity likeVideo(@PathVariable("videoId") Long videoId) {
        return videoService.likeVideo(videoId);
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

    @PostMapping("deleteLike/{videoId}")
    public ResponseEntity deleteLike(@PathVariable("videoId") final Long videoId) {
        return videoService.deleteLike(videoId);
    }

    @GetMapping("getVideosByChannelName/{channelName}")
    public List<VideoForHomeDto> getVideosByChannelName(@PathVariable("channelName") final String channelName){
        return videoService.getVideoByChannelName(channelName);
    }

}
