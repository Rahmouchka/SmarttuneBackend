package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Chanson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChansonRepository extends JpaRepository<Chanson, Long> {
    List<Chanson> findByArtisteId(Long artisteId);
    @Query("SELECT c FROM Chanson c WHERE LOWER(c.humeur) = LOWER(:humeur) ORDER BY RAND()")
    List<Chanson> findRandomByHumeur(@Param("humeur") String humeur, Pageable pageable);
}