package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.ArtistRequestRepository;
import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.ArtistRegistrationRequest;
import com.example.SmarttuneBackend.dto.UserRegistrationRequest;
import com.example.SmarttuneBackend.entities.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final String UPLOAD_DIR = "uploads/artists/";

    @Autowired private UserRepository userRepository;
    @Autowired private ArtistRequestRepository artistRequestRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    private Path uploadDirectory;

    @PostConstruct
    public void init() {
        String projectDir = System.getProperty("user.dir");
        uploadDirectory = Paths.get(projectDir, UPLOAD_DIR);

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload : " + uploadDirectory, e);
        }
    }

    // INSCRIPTION UTILISATEUR CLASSIQUE
    public User registerUser(UserRegistrationRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail()) ||
                artistRequestRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setNumTel(dto.getNumTel());
        user.setGenre(dto.getGenre());
        user.setDateNaissance(dto.getDateNaissance());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getPrenom() + " " + saved.getNom());

        return saved;
    }

    // INSCRIPTION ARTISTE (demande en attente)
    public ArtistRequest registerArtist(ArtistRegistrationRequest dto, MultipartFile pdf) throws IOException {
        if (userRepository.existsByEmail(dto.getEmail()) ||
                artistRequestRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Sauvegarde du PDF
        String originalFilename = pdf.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Le fichier doit être un PDF");
        }

        String fileName = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadDirectory.resolve(fileName);
        Files.write(filePath, pdf.getBytes());

        // Création de la demande
        ArtistRequest request = new ArtistRequest();
        request.setNomArtiste(dto.getUsername());        // nom de scène
        request.setNom(dto.getNom());
        request.setPrenom(dto.getPrenom());
        request.setEmail(dto.getEmail());
        request.setNumTel(dto.getNumTel());
        request.setGenre(dto.getGenre());
        request.setDateNaissance(dto.getDateNaissance()); // ← CORRIGÉ : plus age
        request.setBio(dto.getBio());
        request.setPdfPath(filePath.toString());
        request.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        request.setStatus(ArtistStatus.PENDING);
        request.setSubmittedAt(LocalDateTime.now());

        ArtistRequest saved = artistRequestRepository.save(request);

        // Notifications
        emailService.sendArtistPendingEmail(saved.getEmail(), saved.getPrenom());
        emailService.sendAdminNewArtistRequest(saved.getId(), dto.getUsername(), saved.getEmail());

        return saved;
    }

    // APPROBATION PAR L'ADMIN
    public User approveArtist(Long requestId) {
        ArtistRequest request = artistRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande d'artiste non trouvée"));

        if (request.getStatus() != ArtistStatus.PENDING) {
            throw new RuntimeException("Cette demande a déjà été traitée");
        }

        // Création de l'utilisateur Artiste à partir de la demande
        Artiste artiste = new Artiste();
        artiste.setUsername(request.getNomArtiste());
        artiste.setNom(request.getNom());
        artiste.setPrenom(request.getPrenom());
        artiste.setEmail(request.getEmail());
        artiste.setNumTel(request.getNumTel());
        artiste.setGenre(request.getGenre());
        artiste.setDateNaissance(request.getDateNaissance()); // ← CORRIGÉ
        artiste.setPassword(request.getPasswordHash()); // déjà hashé
        artiste.setRole(Role.ARTIST);
        artiste.setBio(request.getBio());
        artiste.setNomArtiste(request.getNomArtiste());
        artiste.setActive(true);

        User saved = userRepository.save(artiste);

        // Mise à jour du statut de la demande
        request.setStatus(ArtistStatus.APPROVED);
        artistRequestRepository.save(request);

        emailService.sendArtistApprovedEmail(artiste.getEmail(), artiste.getPrenom());

        return saved;
    }

    // REJET PAR L'ADMIN
    public void rejectArtist(Long requestId) {
        ArtistRequest request = artistRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande d'artiste non trouvée"));

        if (request.getStatus() == ArtistStatus.PENDING) {
            // Suppression du PDF si existant
            if (request.getPdfPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(request.getPdfPath()));
                } catch (IOException e) {
                    // Log en production, mais on ne bloque pas le rejet
                    System.err.println("Erreur suppression PDF : " + e.getMessage());
                }
            }

            request.setStatus(ArtistStatus.REJECTED);
            artistRequestRepository.save(request);

            emailService.sendArtistRejectedEmail(request.getEmail(), request.getPrenom());
        }
        // Si déjà traité, on ne fait rien (ou on peut lever une exception selon besoin)
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!user.isActive()) {
            throw new RuntimeException("Compte désactivé");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        return user;
    }
}