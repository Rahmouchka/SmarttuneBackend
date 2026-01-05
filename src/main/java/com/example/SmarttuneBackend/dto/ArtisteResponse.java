package com.example.SmarttuneBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtisteResponse {
    private Long id;
    private String nomArtiste;
    private String prenom;
    private String nom;
    private String bio;
    private String email;
    private Integer nbrAbonnees;
    private Integer nbrAbonnements;
}