package com.bogdan.video.microservice.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoDetailsForNonUsers {

    private String videoTitle;
    private String description;
    private String videoChannelName;

}
