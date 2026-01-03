package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Tous les albums d'un artiste
    List<Album> findByArtiste(Artiste artiste);

    // Tous les albums d'un artiste par ID
    List<Album> findByArtisteId(Long artisteId);

    // Recherche par titre (insensible à la casse)
    List<Album> findByTitreContainingIgnoreCase(String titre);

    // Albums sortis après une certaine date
    List<Album> findByDateSortieAfter(java.time.LocalDate date);

    // Combiner artiste + ordre chronologique
    List<Album> findByArtisteIdOrderByDateSortieDesc(Long artisteId);
}
