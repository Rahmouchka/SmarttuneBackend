// src/main/java/com/example/SmarttuneBackend/metier/UserService.java

package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.ProfileUpdateRequest;
import com.example.SmarttuneBackend.dto.UserProfileResponse;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Genre;
import com.example.SmarttuneBackend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // === Récupérer le profil de l'utilisateur connecté ===
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return mapToProfileResponse(user);
    }

    // === Mettre à jour le profil de l'utilisateur connecté ===
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        // Mise à jour des champs communs
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername().trim());
        }
        if (request.getNom() != null) {
            user.setNom(request.getNom().trim());
        }
        if (request.getPrenom() != null) {
            user.setPrenom(request.getPrenom().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!user.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new RuntimeException("Cet email est déjà utilisé par un autre compte");
                }
            }
            user.setEmail(newEmail);
        }
        if (request.getNumTel() != null) {
            String tel = request.getNumTel().trim();
            user.setNumTel(tel.isEmpty() ? null : tel);
        }
        if (request.getBio() != null) {
            String bio = request.getBio().trim();
            user.setBio(bio.isEmpty() ? null : bio);
        }

        // === Champs spécifiques aux ARTISTES ===
        if (user instanceof Artiste artiste) {
            if (request.getNomArtiste() != null && !request.getNomArtiste().isBlank()) {
                artiste.setNomArtiste(request.getNomArtiste().trim());
            }
            // La bio est déjà mise à jour au-dessus (prioritaire pour l'artiste)
        }

        // === Champs spécifiques aux USERS normaux (non artistes) ===
        if (!(user instanceof Artiste)) {
            if (request.getGenre() != null && !request.getGenre().isBlank()) {
                try {
                    user.setGenre(Genre.valueOf(request.getGenre().trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Genre invalide. Valeurs possibles : MASCULIN, FEMININ, AUTRE");
                }
            }
            if (request.getDateNaissance() != null && !request.getDateNaissance().isBlank()) {
                try {
                    user.setDateNaissance(LocalDate.parse(request.getDateNaissance().trim()));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Format de date invalide. Utilisez yyyy-MM-dd");
                }
            }
        }

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    // === Mapper User → DTO de réponse ===
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse dto = new UserProfileResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setNumTel(user.getNumTel());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole().name());

        if (user instanceof Artiste artiste) {
            dto.setType("ARTIST");
            dto.setNomArtiste(artiste.getNomArtiste());
            dto.setNbrAbonnees(artiste.getNbrAbonnees() != null ? artiste.getNbrAbonnees() : 0);
        } else {
            dto.setType("USER");
            dto.setGenre(user.getGenre() != null ? user.getGenre().name() : null);
            dto.setDateNaissance(user.getDateNaissance());
        }

        return dto;
    }
}