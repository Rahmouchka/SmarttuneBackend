package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dto.ChansonStatsResponse;
import com.example.SmarttuneBackend.dto.RatingRequest;
import com.example.SmarttuneBackend.dto.RatingResponse;
import com.example.SmarttuneBackend.metier.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // ========================
    // ÉVALUER UNE CHANSON (créer ou modifier)
    // ========================
    @PostMapping("/{userId}/ratings/chansons/{chansonId}")
    public ResponseEntity<Map<String, Object>> rateChanson(
            @PathVariable Long userId,
            @PathVariable Long chansonId,
            @RequestBody RatingRequest request) {

        try {
            RatingResponse response = ratingService.rateOrUpdateChanson(userId, chansonId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Chanson évaluée avec succès");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========================
    // SUPPRIMER UN RATING
    // ========================
    @DeleteMapping("/{userId}/ratings/chansons/{chansonId}")
    public ResponseEntity<Map<String, Object>> deleteRating(
            @PathVariable Long userId,
            @PathVariable Long chansonId) {

        try {
            ratingService.deleteRating(userId, chansonId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Rating supprimé avec succès");

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========================
    // OBTENIR LE RATING D'UN USER POUR UNE CHANSON
    // ========================
    @GetMapping("/{userId}/ratings/chansons/{chansonId}")
    public ResponseEntity<Map<String, Object>> getMyRating(
            @PathVariable Long userId,
            @PathVariable Long chansonId) {

        try {
            RatingResponse response = ratingService.getUserRatingForChanson(userId, chansonId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Pas d'évaluation trouvée");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========================
    // OBTENIR TOUS LES RATINGS D'UN USER
    // ========================
    @GetMapping("/{userId}/ratings")
    public ResponseEntity<Map<String, Object>> getMyRatings(
            @PathVariable Long userId) {

        try {
            List<RatingResponse> ratings = ratingService.getUserRatings(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", ratings.size());
            result.put("data", ratings);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========================
    // STATISTIQUES D'UNE CHANSON
    // ========================
    @GetMapping("/chansons/{chansonId}/stats")
    public ResponseEntity<Map<String, Object>> getChansonStats(
            @PathVariable Long chansonId) {

        try {
            Map<String, Object> stats = ratingService.getChansonStats(chansonId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ========================
    // STATISTIQUES POUR LES CHANSONS D'UN ARTISTE (Dashboard)
    // ========================
    @GetMapping("/{artisteId}/chansons/stats")
    public ResponseEntity<Map<String, Object>> getArtisteStats(
            @PathVariable Long artisteId) {

        try {
            List<ChansonStatsResponse> stats = ratingService.getArtisteStats(artisteId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", stats.size());
            result.put("data", stats);

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

