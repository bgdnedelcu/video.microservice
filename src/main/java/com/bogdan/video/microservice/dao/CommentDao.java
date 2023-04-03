package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentDao extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoId(Long VideoId);
}