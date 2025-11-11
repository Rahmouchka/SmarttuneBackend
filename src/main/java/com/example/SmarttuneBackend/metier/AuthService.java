package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dao.ArtistRequestRepository;
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

    @Autowired private UserRepository userRepository;
    @Autowired private ArtistRequestRepository artistRequestRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    private String UPLOAD_DIR;

    @PostConstruct
    public void init() {
        UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/artists/";
        Path path = Paths.get(UPLOAD_DIR);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Impossible de créer le dossier d'upload", e);
            }
        }
    }

    // INSCRIPTION UTILISATEUR
    public User registerUser(UserRegistrationRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail()) ||
                artistRequestRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setNumTel(dto.getNumTel());
        user.setGenre(dto.getGenre());
        user.setAge(dto.getAge());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getPrenom());
        return saved;
    }

    // INSCRIPTION ARTISTE
    public ArtistRequest registerArtist(ArtistRegistrationRequest dto, MultipartFile pdf) throws IOException {
        if (userRepository.existsByEmail(dto.getEmail()) ||
                artistRequestRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        String fileName = UUID.randomUUID() + "_" + pdf.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(uploadPath.getParent());
        Files.write(uploadPath, pdf.getBytes());

        ArtistRequest request = new ArtistRequest();
        request.setNomArtiste(dto.getUsername());
        request.setNom(dto.getNom());
        request.setPrenom(dto.getPrenom());
        request.setEmail(dto.getEmail());
        request.setNumTel(dto.getNumTel());
        request.setGenre(dto.getGenre());
        request.setAge(dto.getAge());
        request.setBio(dto.getBio());
        request.setPdfPath(uploadPath.toString());
        request.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        request.setStatus(ArtistStatus.PENDING);
        request.setSubmittedAt(LocalDateTime.now());

        ArtistRequest saved = artistRequestRepository.save(request);

        emailService.sendArtistPendingEmail(saved.getEmail(), saved.getPrenom());
        emailService.sendAdminNewArtistRequest(saved.getId(), dto.getUsername(), saved.getEmail());

        return saved;
    }

    // APPROBATION ARTISTE
    public User approveArtist(Long requestId) {
        ArtistRequest request = artistRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (request.getStatus() != ArtistStatus.PENDING) {
            throw new RuntimeException("Demande déjà traitée");
        }

        User artiste = new User();
        artiste.setUsername(request.getNomArtiste());
        artiste.setNom(request.getNom());
        artiste.setPrenom(request.getPrenom());
        artiste.setEmail(request.getEmail());
        artiste.setNumTel(request.getNumTel());
        artiste.setGenre(request.getGenre());
        artiste.setAge(request.getAge());
        artiste.setPassword(request.getPasswordHash()); // déjà hashé
        artiste.setRole(Role.ARTIST);
        artiste.setBio(request.getBio());
        artiste.setActive(true);

        User saved = userRepository.save(artiste);
        request.setStatus(ArtistStatus.APPROVED);
        artistRequestRepository.save(request);

        emailService.sendArtistApprovedEmail(artiste.getEmail(), artiste.getPrenom());
        return saved;
    }

    // REJET
    public void rejectArtist(Long requestId) {
        ArtistRequest request = artistRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (request.getPdfPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(request.getPdfPath()));
            } catch (IOException e) {
                // log
            }
        }

        request.setStatus(ArtistStatus.REJECTED);
        artistRequestRepository.save(request);

        emailService.sendArtistRejectedEmail(request.getEmail(), request.getPrenom());
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
        //return jwtUtil.generateToken(user);
    }
}