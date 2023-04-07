package com.bogdan.video.microservice.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoForHomeDto {
    private Long videoId;
    private String videoTitle;
    private String videoChannel;
}
