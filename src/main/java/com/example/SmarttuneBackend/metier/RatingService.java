package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dao.RatingRepository;
import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.ChansonStatsResponse;
import com.example.SmarttuneBackend.dto.RatingRequest;
import com.example.SmarttuneBackend.dto.RatingResponse;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.Rating;
import com.example.SmarttuneBackend.entities.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ChansonRepository chansonRepository;

    // ========================
    // CRÉER/MODIFIER UN RATING
    // ========================
    @Transactional
    public RatingResponse rateOrUpdateChanson(Long userId, Long chansonId, RatingRequest request) {
        // Validation
        if (!request.isValid()) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }

        // Vérifications
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée: " + chansonId));

        // Vérifier si un rating existe déjà
        Rating rating = ratingRepository.findByUserIdAndChansonId(userId, chansonId)
                .orElseGet(() -> Rating.builder()
                        .user(user)
                        .chanson(chanson)
                        .build());

        // Mise à jour de la note et du timestamp de modification
        rating.setNote(request.getNote());
        rating.setDateModification(LocalDateTime.now());

        Rating saved = ratingRepository.save(rating);

        return convertToResponse(saved);
    }

    // ========================
    // SUPPRIMER UN RATING
    // ========================
    @Transactional
    public void deleteRating(Long userId, Long chansonId) {
        Rating rating = ratingRepository.findByUserIdAndChansonId(userId, chansonId)
                .orElseThrow(() -> new RuntimeException("Rating non trouvé pour cet utilisateur et cette chanson"));

        ratingRepository.delete(rating);
    }

    // ========================
    // RÉCUPÉRER LE RATING D'UN USER
    // ========================
    public RatingResponse getUserRatingForChanson(Long userId, Long chansonId) {
        Rating rating = ratingRepository.findByUserIdAndChansonId(userId, chansonId)
                .orElseThrow(() -> new RuntimeException("Cet utilisateur n'a pas évalué cette chanson"));

        return convertToResponse(rating);
    }

    // ========================
    // STATISTIQUES D'UNE CHANSON
    // ========================
    public Map<String, Object> getChansonStats(Long chansonId) {
        // Vérification que la chanson existe
        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée: " + chansonId));

        Double moyenne = ratingRepository.getAverageRatingByChansonId(chansonId).orElse(0.0);
        Long totalRatings = ratingRepository.countRatingsByChansonId(chansonId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("chansonId", chansonId);
        stats.put("titre", chanson.getTitre());
        stats.put("moyenneNote", formatNote(moyenne));
        stats.put("nombreEvaluations", totalRatings);
        stats.put("artisteId", chanson.getArtiste().getId());

        return stats;
    }

    // ========================
    // STATISTIQUES POUR TOUTES LES CHANSONS D'UN ARTISTE
    // ========================
    public List<ChansonStatsResponse> getArtisteStats(Long artisteId) {
        List<Object[]> results = ratingRepository.getArtisteChansonStats(artisteId);

        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        return results.stream()
                .map(row -> {
                    Long chansonId = ((Number) row[0]).longValue();
                    Chanson chanson = chansonRepository.findById(chansonId)
                            .orElse(null);

                    if (chanson == null) return null;

                    Double moyenne = ((Number) row[1]).doubleValue();
                    Long count = ((Number) row[2]).longValue();

                    return ChansonStatsResponse.builder()
                            .chansonId(chansonId)
                            .titre(chanson.getTitre())
                            .moyenneNote(formatNote(moyenne))
                            .nombreEvaluations(count)
                            .build();
                })
                .filter(stat -> stat != null)
                .sorted((a, b) -> b.getMoyenneNote().compareTo(a.getMoyenneNote())) // Tri décroissant
                .collect(Collectors.toList());
    }

    // ========================
    // VÉRIFIER SI UN USER A DÉJÀ ÉVALUÉ UNE CHANSON
    // ========================
    public boolean hasUserRatedChanson(Long userId, Long chansonId) {
        return ratingRepository.existsByUserIdAndChansonId(userId, chansonId);
    }

    // ========================
    // OBTENIR TOUS LES RATINGS D'UN USER
    // ========================
    public List<RatingResponse> getUserRatings(Long userId) {
        return ratingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========================
    // OBTENIR TOUS LES RATINGS D'UNE CHANSON
    // ========================
    public List<RatingResponse> getChansonRatings(Long chansonId) {
        return ratingRepository.findByChansonId(chansonId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========================
    // CONVERSIONS & UTILITAIRES
    // ========================
    private RatingResponse convertToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .chansonId(rating.getChanson().getId())
                .chansonTitre(rating.getChanson().getTitre())
                .note(rating.getNote())
                .dateCreation(rating.getDateCreation())
                .dateModification(rating.getDateModification())
                .build();
    }

    // Formater la moyenne à 1 décimale
    private Double formatNote(Double note) {
        if (note == null || note == 0.0) return 0.0;
        return Math.round(note * 10.0) / 10.0;
    }
}

