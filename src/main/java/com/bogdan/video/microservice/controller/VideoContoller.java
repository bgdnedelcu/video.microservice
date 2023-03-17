package com.bogdan.video.microservice.controller;

import com.bogdan.video.microservice.constants.AppConstants;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("videoplatform/api/video")
public class VideoContoller {


//    @RequestMapping("video1")
//    public ResponseEntity returnVideo(){
//        return ResponseEntity.ok().body("salut");
//    }

    @RequestMapping(value = {"play/{videoUrl}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<FileSystemResource> playVideo(@PathVariable("videoUrl") final String videoUrl)
            throws FileNotFoundException {
        final String EXTENSION = ".mp4";
        return Mono.fromSupplier(() -> new FileSystemResource(AppConstants.STORAGE_PATH + videoUrl + EXTENSION));
    }

}
