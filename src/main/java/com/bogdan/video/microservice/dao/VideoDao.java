package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VideoDao extends JpaRepository<Video, Long> {

     Page<Video> findAll(Pageable pageable);
     List<Video> findAll();
     Video findVideoTitleById(Long id);


     @Query("select v from Video v where v.title like %:text% or v.description like %:text%")
     public List<Video> findByTitleOrDescription(final String text);

}