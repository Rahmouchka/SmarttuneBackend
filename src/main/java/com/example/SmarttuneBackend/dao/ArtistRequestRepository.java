package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.ArtistRequest;
import com.example.SmarttuneBackend.entities.ArtistStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRequestRepository extends JpaRepository<ArtistRequest, Long> {
    boolean existsByEmail(String email);

    // MÉTHODES POUR L'ADMIN
    List<ArtistRequest> findByStatus(ArtistStatus status);
    long countByStatus(ArtistStatus status);
    List<ArtistRequest> findByStatusOrderBySubmittedAtAsc(ArtistStatus status);
}