// src/main/java/com/example/SmarttuneBackend/dto/UserProfileResponse.java
package com.example.SmarttuneBackend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private String numTel;
    private String bio;
    private String role; // "USER" ou "ARTIST"
    private String type; // "USER" ou "ARTIST"

    // Pour USER
    private String genre;
    private LocalDate dateNaissance;

    // Pour ARTIST
    private String nomArtiste;
    private Integer nbrAbonnees;
}