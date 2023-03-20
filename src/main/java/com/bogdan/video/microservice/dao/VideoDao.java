package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoDao extends JpaRepository<Video, Long> {
}