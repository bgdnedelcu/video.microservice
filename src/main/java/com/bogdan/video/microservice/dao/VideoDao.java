package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoDao extends JpaRepository<Video, Long> {

    public Page<Video> findAll(Pageable pageable);
    public List<Video> findAll();
    public Video findVideoTitleById(Long id);

}