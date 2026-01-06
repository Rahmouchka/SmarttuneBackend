package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Récupérer le rating d'un user pour une chanson
    Optional<Rating> findByUserIdAndChansonId(Long userId, Long chansonId);

    // Tous les ratings d'une chanson
    List<Rating> findByChansonId(Long chansonId);

    // Tous les ratings d'un user
    List<Rating> findByUserId(Long userId);

    // Moyenne des notes pour une chanson (NULL si pas de ratings)
    @Query("SELECT AVG(CAST(r.note AS DOUBLE)) FROM Rating r WHERE r.chanson.id = :chansonId")
    Optional<Double> getAverageRatingByChansonId(@Param("chansonId") Long chansonId);

    // Nombre total de ratings pour une chanson
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.chanson.id = :chansonId")
    Long countRatingsByChansonId(@Param("chansonId") Long chansonId);

    // Vérifier si un user a déjà évalué une chanson
    boolean existsByUserIdAndChansonId(Long userId, Long chansonId);

    // Statistiques pour toutes les chansons d'un artiste
    @Query("SELECT r.chanson.id, AVG(CAST(r.note AS DOUBLE)), COUNT(r) " +
           "FROM Rating r " +
           "WHERE r.chanson.artiste.id = :artisteId " +
           "GROUP BY r.chanson.id")
    List<Object[]> getArtisteChansonStats(@Param("artisteId") Long artisteId);
}

