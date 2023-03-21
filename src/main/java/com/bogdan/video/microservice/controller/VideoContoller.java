package com.bogdan.video.microservice.controller;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.dao.VideoDao;
import com.bogdan.video.microservice.service.VideoService;
import com.bogdan.video.microservice.view.Video;
import com.bogdan.video.microservice.view.dto.PlayListVideo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("videoplatform/api/video")
public class VideoContoller {

    private VideoService videoService;
    private VideoDao videoDao;

    public VideoContoller(VideoService videoService, VideoDao videoDao){
        this.videoService = videoService;
        this.videoDao = videoDao;
    }

    @GetMapping("home")
    public List<Video> loadVideos(Pageable pageable) {
        Page<Video> videos = videoService.loadVideos(pageable);
        return videos.getContent();
    }

    @GetMapping("home/allVideos")
    public List<Video> allVideos(){
        return videoService.getAllVideos();
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
        return videoService.addVideoToPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
    }

    @PostMapping("removeVideoFromPlaylist")
    public ResponseEntity removeVideoFromPlayList(@RequestBody final PlayListVideo playListVideo){
        return videoService.removeVideoFromPlaylist(playListVideo.getIdVideo(), playListVideo.getIdPlayList());
    }

    @GetMapping("playList/{idplayList}")
    public List<Video> getVideosFromPlayList(@PathVariable("idplayList") final Long idPlayList) {
        return videoService.getVideosFromPlayList(idPlayList);
    }

    @GetMapping("videoTitle/{videoId}")
    public String findVideoTitleByVideoId(@PathVariable("videoId") final Long videoId){
        return videoService.findVideoTitleByVideoId(videoId);
    }
}
