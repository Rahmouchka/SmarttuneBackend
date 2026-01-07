// src/main/java/com/example/SmarttuneBackend/dto/ProfileUpdateRequest.java
package com.example.SmarttuneBackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(max = 50, message = "Le nom d'utilisateur ne peut pas dépasser 50 caractères")
    private String username;

    @Size(max = 50)
    private String nom;

    @Size(max = 50)
    private String prenom;

    @Email(message = "Email invalide")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
    private String numTel;

    @Size(max = 500)
    private String bio;

    // Champ spécifique aux artistes
    private String nomArtiste;

    // Optionnel : pour les users normaux (si tu veux permettre la modification)
    private String genre; // "MASCULIN", "FEMININ", "AUTRE"
    private String dateNaissance; // format "yyyy-MM-dd"
}