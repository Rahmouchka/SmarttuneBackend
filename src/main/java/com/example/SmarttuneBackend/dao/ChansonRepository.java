package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Chanson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChansonRepository extends JpaRepository<Chanson, Long> {
    List<Chanson> findByArtisteId(Long artisteId);

}