package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.VideoLikes;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeDao extends JpaRepository<VideoLikes, Long> {

    @Modifying
    @Transactional
    @Query(value = "delete from video_likes where video_id = :videoId and user_id = :userId", nativeQuery = true)
    void deleteLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

}
