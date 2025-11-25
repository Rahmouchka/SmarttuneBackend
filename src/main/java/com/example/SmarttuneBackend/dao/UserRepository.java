package com.example.SmarttuneBackend.dao;

import com.example.SmarttuneBackend.entities.Role;
import com.example.SmarttuneBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Long countByRole(Role role);
    List<User> findByRole(Role role);
}