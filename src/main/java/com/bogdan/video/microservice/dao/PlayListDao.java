package com.bogdan.video.microservice.dao;

import com.bogdan.video.microservice.view.PlayList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayListDao extends JpaRepository<PlayList, Long> {
    @Modifying
    @Transactional
    @Query(value = "INSERT into playlist_video(video_id, playlist_id) values(:idvideo, :idplay)", nativeQuery = true)
    int insertPlayListVideo(@Param("idvideo") Long idVideo, @Param("idplay") final Long idPlay);

    @Modifying
    @Transactional
    @Query(value = "DELETE from playlist_video where video_id = :idvideo and playlist_id = :idplay", nativeQuery = true)
    int deleteVideoFromPlaylist(@Param("idvideo") Long idVideo, @Param("idplay") final Long idPlay);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM playlist_video WHERE playlist_id = :idPlay", nativeQuery = true)
    int deleteAllVideosFromPlaylist(@Param("idPlay") Long idPlay);

    @Query(value = "SELECT COUNT(*) FROM playlist_video WHERE video_id = :idvideo AND playlist_id = :idplay", nativeQuery = true)
    int checkIfExistsRecords(@Param("idvideo") final Long idVideo, @Param("idplay") final Long idPlay);

}