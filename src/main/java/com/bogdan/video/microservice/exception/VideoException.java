package com.bogdan.video.microservice.exception;

import com.bogdan.video.microservice.view.Video;

public class VideoException extends Exception{

    public VideoException(final String message){
        super(message);
    }
}
