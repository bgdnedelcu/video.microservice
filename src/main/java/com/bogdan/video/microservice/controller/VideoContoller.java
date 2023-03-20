package com.bogdan.video.microservice.controller;

import com.bogdan.video.microservice.constants.AppConstants;
import com.bogdan.video.microservice.service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("videoplatform/api/video")
public class VideoContoller {

    private VideoService videoService;

    public VideoContoller(VideoService videoService){
        this.videoService = videoService;
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

}
