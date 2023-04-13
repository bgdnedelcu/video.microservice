package com.bogdan.video.microservice.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoCommentDto {

    private Long idComment;
    private Long idUser;
    private String channelName;
    private String comment;

}
