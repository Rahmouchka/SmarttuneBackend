package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Artiste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtisteRepository extends JpaRepository<Artiste, Long> {

    // Trouver un artiste par son email (utile pour login)
    Optional<Artiste> findByEmail(String email);

    // Trouver un artiste par son nom d'artiste (pour recherche)
    Optional<Artiste> findByNomArtisteIgnoreCase(String nomArtiste);

    // Vérifier si un nom d'artiste existe déjà
    boolean existsByNomArtisteIgnoreCase(String nomArtiste);

    // Récupérer les artistes les plus suivis (top 10)
    @Query("SELECT a FROM Artiste a ORDER BY a.nbrAbonnees DESC")
    List<Artiste> findTopArtistes(int limit);

    // Version avec Pageable si tu veux pagination plus tard
    // Page<Artiste> findAllByOrderByNbrAbonneesDesc(Pageable pageable);
}