package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Favoris;
import com.example.SmarttuneBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavorisRepository extends JpaRepository<Favoris, Long> {
    Optional<Favoris> findByUser(User user);
}