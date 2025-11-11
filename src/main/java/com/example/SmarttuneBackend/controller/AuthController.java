package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dto.ArtistRegistrationRequest;
import com.example.SmarttuneBackend.dto.LoginRequest;
import com.example.SmarttuneBackend.dto.UserRegistrationRequest;
import com.example.SmarttuneBackend.entities.ArtistRequest;
import com.example.SmarttuneBackend.entities.Genre;
import com.example.SmarttuneBackend.entities.User;
import com.example.SmarttuneBackend.metier.AuthService;
import com.example.SmarttuneBackend.metier.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private PasswordResetService passwordResetService;

    // INSCRIPTION USER
    @PostMapping("/register/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest dto) {
        return ResponseEntity.ok(authService.registerUser(dto));
    }

    // INSCRIPTION ARTISTE + PDF
    @PostMapping(value = "/register/artist", consumes = "multipart/form-data")
    public ResponseEntity<ArtistRequest> registerArtist(
            @RequestParam("username") String username,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam(value = "numTel", required = false) String numTel,
            @RequestParam("age") Integer age,
            @RequestParam("genre") String genreStr,  // "H" ou "F"
            @RequestParam("password") String password,
            @RequestParam("bio") String bio,
            @RequestParam("pdf") MultipartFile pdf) throws IOException {

        // Valider champs
        if (username == null || username.isBlank() ||
                nom == null || nom.isBlank() ||
                prenom == null || prenom.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank() ||
                bio == null || bio.length() < 50 ||
                age == null || age < 13 || age > 100) {
            return ResponseEntity.badRequest().body(null);
        }

        // Convertir "H"/"F" → Genre.H / Genre.F
        Genre genre;
        if ("H".equalsIgnoreCase(genreStr)) {
            genre = Genre.H;
        } else if ("F".equalsIgnoreCase(genreStr)) {
            genre = Genre.F;
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        // Valider PDF
        if (pdf == null || pdf.isEmpty() || !pdf.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(null);
        }

        // Créer DTO
        ArtistRegistrationRequest dto = new ArtistRegistrationRequest();
        dto.setUsername(username);
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setEmail(email);
        dto.setNumTel(numTel);
        dto.setAge(age);
        dto.setGenre(genre);
        dto.setPassword(password);
        dto.setBio(bio);

        return ResponseEntity.ok(authService.registerArtist(dto, pdf));
    }
    // CONNEXION (à améliorer avec JWT plus tard)
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest login) {
        User user = authService.login(login.getEmail(), login.getPassword());
        return ResponseEntity.ok(user);
    }

    // ADMIN : APPROUVER
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<User> approveArtist(@PathVariable Long id) {
        return ResponseEntity.ok(authService.approveArtist(id));
    }

    // ADMIN : REJETER
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<String> rejectArtist(@PathVariable Long id) {
        authService.rejectArtist(id);
        return ResponseEntity.ok("Artiste rejeté avec succès");
    }

    // MOT DE PASSE OUBLIÉ
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            passwordResetService.requestPasswordReset(email);
            return ResponseEntity.ok("Email de réinitialisation envoyé");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Mot de passe réinitialisé");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
        return ResponseEntity.ok(passwordResetService.validateToken(token));
    }
}

