package com.example.SmarttuneBackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "artist_requests")
public class ArtistRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nomArtiste;
    @Column
    private String nom;
    @Column
    private String prenom;
    @Column
    private String email;
    @Column
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column
    private Genre genre;
    @Column
    private LocalDate dateNaissance;
    @Column
    private String numTel;
    @Column
    private String bio;
    @Column
    private String pdfPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtistStatus status = ArtistStatus.PENDING;

    @Column
    private LocalDateTime submittedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public String getNomArtiste() {
        return nomArtiste;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    public Genre getGenre() {
        return genre;
    }

    public String getNumTel() {
        return numTel;
    }

    public String getBio() {
        return bio;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public ArtistStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNomArtiste(String nomArtiste) {
        this.nomArtiste = nomArtiste;
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

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }



    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void setStatus(ArtistStatus status) {
        this.status = status;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}