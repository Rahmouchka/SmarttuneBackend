package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtisteId(Long artisteId);

    @Query("SELECT a FROM Album a WHERE a.id = :albumId AND a.artiste.id = :artisteId")
    Optional<Album> findByIdAndArtisteId(@Param("albumId") Long albumId, @Param("artisteId") Long artisteId);

    List<Album> findByArtisteIdOrderByDateSortieDesc(Long artisteId);
}