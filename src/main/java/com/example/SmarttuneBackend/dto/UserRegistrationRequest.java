package com.example.SmarttuneBackend.dto;
import com.example.SmarttuneBackend.entities.Genre;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 30, message = "3 à 30 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Caractères autorisés : lettres, chiffres, _, -")
    private String username;

    @NotBlank @Size(min = 2, max = 50)
    private String nom;

    @NotBlank @Size(min = 2, max = 50)
    private String prenom;

    @Email @NotBlank
    private String email;

    @Pattern(regexp = "^(\\+216|00216)?[0-9]{8}$", message = "Numéro tunisien invalide")
    private String numTel;

    @Min(13) @Max(100)
    private Integer age;

    private Genre genre;

    @NotBlank
    @Size(min = 8, message = "Minimum 8 caractères")
    @Pattern.List({
            @Pattern(regexp = ".*[A-Z].*", message = "Au moins une majuscule"),
            @Pattern(regexp = ".*[a-z].*", message = "Au moins une minuscule"),
            @Pattern(regexp = ".*[0-9].*", message = "Au moins un chiffre"),
            @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "Au moins un caractère spécial")
    })
    private String password;

    public String getUsername() {
        return username;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getNumTel() {
        return numTel;
    }

    public Integer getAge() {
        return age;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getPassword() {
        return password;
    }
}