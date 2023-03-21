package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.PlayList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlayListDao extends JpaRepository<PlayList, Long> {

    public Optional<PlayList> findById(Long id);
    @Modifying
    @Transactional
    @Query(value = "INSERT into playlist_video(video_id, playlist_id) values(:idvideo, :idplay)", nativeQuery = true)
    public void insertPlayListVideo(@Param("idvideo") Long idVideo, @Param("idplay") final Long idPlay);

    @Modifying
    @Transactional
    @Query(value = "DELETE from playlist_video where video_id = :idvideo and playlist_id = :idplay", nativeQuery = true)
    public void deletePlayListVideo(@Param("idvideo") Long idVideo, @Param("idplay") final Long idPlay);

}