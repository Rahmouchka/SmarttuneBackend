package com.example.SmarttuneBackend.dto;

import com.example.SmarttuneBackend.entities.Genre;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ArtistRegistrationRequest {

    @NotBlank @Size(min = 3, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nom d'artiste invalide")
    private String username; // nom de scène

    @NotBlank @Size(min = 2, max = 50)
    private String nom;

    @NotBlank @Size(min = 2, max = 50)
    private String prenom;

    @Email @NotBlank
    private String email;

    @Pattern(regexp = "^(\\+216|00216)?[0-9]{8}$")
    private String numTel;

    @Min(13) @Max(100)
    private Integer age;

    private Genre genre;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank
    @Size(min = 50, max = 1000, message = "Bio : 50 à 1000 caractères")
    private String bio;

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

    public String getBio() {
        return bio;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}