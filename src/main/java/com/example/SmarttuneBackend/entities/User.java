package com.example.SmarttuneBackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;
    @Column
    private String nom;
    @Column
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String numTel;

    @Enumerated(EnumType.STRING)
    @Column
    private Genre genre;
    @Column
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private String password;

    @Column
    private LocalDate dateInscription = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column
    private String bio;
    @Column
    private Integer nbrAbonnements;
    @Column
    private boolean isActive = true;


    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
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

    public Genre getGenre() {
        return genre;
    }



    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public Integer getNbrAbonnements() {
        return nbrAbonnements;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
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

    public void setGenre(Genre genre) {
        this.genre = genre;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setNbrAbonnements(Integer nbrAbonnements) {
        this.nbrAbonnements = nbrAbonnements;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}