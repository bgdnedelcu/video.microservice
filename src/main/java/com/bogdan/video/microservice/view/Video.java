package com.bogdan.video.microservice.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "user_id")
    private int idUser;

    @JsonIgnore
    @OneToMany(mappedBy = "video")
    private List<Comment> commentsList;

    @JsonIgnore
    @OneToMany(mappedBy = "video")
    private List<VideoLikes> videoLikesList;

}
