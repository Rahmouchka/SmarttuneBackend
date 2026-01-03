package com.example.SmarttuneBackend.dto;

import com.example.SmarttuneBackend.entities.Genre;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ArtistRegistrationRequest {

    @NotBlank(message = "Le nom d'artiste (username) est obligatoire")
    @Size(min = 3, max = 30, message = "Le nom d'artiste doit contenir entre 3 et 30 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nom d'artiste invalide : seuls lettres, chiffres, _ et - autorisés")
    private String username; // nom de scène / nom d'artiste

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^(\\+216|00216)?[0-9]{8}$", message = "Numéro de téléphone tunisien invalide")
    private String numTel;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le genre est obligatoire")
    private Genre genre;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern.List({
            @Pattern(regexp = ".*[A-Z].*", message = "Au moins une majuscule requise"),
            @Pattern(regexp = ".*[a-z].*", message = "Au moins une minuscule requise"),
            @Pattern(regexp = ".*[0-9].*", message = "Au moins un chiffre requis"),
            @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "Au moins un caractère spécial requis")
    })
    private String password;

    @NotBlank(message = "La bio est obligatoire")
    @Size(min = 50, max = 1000, message = "La bio doit contenir entre 50 et 1000 caractères")
    private String bio;

}