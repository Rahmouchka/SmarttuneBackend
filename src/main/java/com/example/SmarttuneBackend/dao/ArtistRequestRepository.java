package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.ArtistRequest;
import com.example.SmarttuneBackend.entities.ArtistStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistRequestRepository extends JpaRepository<ArtistRequest, Long> {
    List<ArtistRequest> findByStatus(ArtistStatus status);
    boolean existsByEmail(String email);
}