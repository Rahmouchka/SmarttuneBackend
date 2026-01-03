package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dto.ArtistRegistrationRequest;
import com.example.SmarttuneBackend.dto.LoginRequest;
import com.example.SmarttuneBackend.dto.UserRegistrationRequest;
import com.example.SmarttuneBackend.entities.ArtistRequest;
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

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    // INSCRIPTION UTILISATEUR CLASSIQUE
    @PostMapping("/register/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest dto) {
        return ResponseEntity.ok(authService.registerUser(dto));
    }

    // INSCRIPTION ARTISTE (avec PDF pour vérification)
    @PostMapping(value = "/register/artist", consumes = "multipart/form-data")
    public ResponseEntity<ArtistRequest> registerArtist(
            @Valid ArtistRegistrationRequest dto,
            @RequestParam("pdf") MultipartFile pdf) throws IOException {

        // Validation du fichier PDF
        if (pdf == null || pdf.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        if (!pdf.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest()
                    .body(new ArtistRequest()); // ou mieux : un objet avec message d'erreur personnalisé
        }

        return ResponseEntity.ok(authService.registerArtist(dto, pdf));
    }

    // CONNEXION
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(user);
    }

    // ADMIN : APPROUVER UNE DEMANDE D'ARTISTE
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<User> approveArtist(@PathVariable Long id) {
        return ResponseEntity.ok(authService.approveArtist(id));
    }

    // ADMIN : REJETER UNE DEMANDE D'ARTISTE
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<String> rejectArtist(@PathVariable Long id) {
        authService.rejectArtist(id);
        return ResponseEntity.ok("Demande d'artiste rejetée avec succès");
    }

    // MOT DE PASSE OUBLIÉ - Demande de réinitialisation
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            passwordResetService.requestPasswordReset(email);
            return ResponseEntity.ok("Email de réinitialisation envoyé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Réinitialisation du mot de passe avec token
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Validation du token de réinitialisation
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
        return ResponseEntity.ok(passwordResetService.validateToken(token));
    }
}