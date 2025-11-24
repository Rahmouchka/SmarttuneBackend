package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.ArtistRequestRepository;
import com.example.SmarttuneBackend.entities.ArtistRequest;
import com.example.SmarttuneBackend.entities.ArtistStatus;
import com.example.SmarttuneBackend.metier.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private ArtistRequestRepository artistRequestRepository;
    @Autowired private AuthService authService;

    // LISTE DES DEMANDES PENDING
    @GetMapping("/artist-requests")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests() {
        List<ArtistRequest> requests = artistRequestRepository.findByStatus(ArtistStatus.PENDING);

        List<Map<String, Object>> response = requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("nomArtiste", req.getNomArtiste());
            map.put("nom", req.getNom());
            map.put("prenom", req.getPrenom());
            map.put("email", req.getEmail());
            map.put("numTel", req.getNumTel());
            map.put("age", req.getAge());
            map.put("genre", req.getGenre());
            map.put("bio", req.getBio());
            map.put("submittedAt", req.getSubmittedAt());
            map.put("status", req.getStatus());

            // CALCULER DÉLAI (urgent si > 2 jours)
            long hoursElapsed = ChronoUnit.HOURS.between(req.getSubmittedAt(), LocalDateTime.now());
            map.put("hoursElapsed", hoursElapsed);
            map.put("isUrgent", hoursElapsed > 48);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // DÉTAILS D'UNE DEMANDE
    @GetMapping("/artist-requests/{id}")
    public ResponseEntity<Map<String, Object>> getRequestDetails(@PathVariable Long id) {
        ArtistRequest req = artistRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", req.getId());
        map.put("nomArtiste", req.getNomArtiste());
        map.put("nom", req.getNom());
        map.put("prenom", req.getPrenom());
        map.put("email", req.getEmail());
        map.put("numTel", req.getNumTel());
        map.put("age", req.getAge());
        map.put("genre", req.getGenre());
        map.put("bio", req.getBio());
        map.put("submittedAt", req.getSubmittedAt());
        map.put("status", req.getStatus());
        map.put("hasPdf", req.getPdfPath() != null);

        long hoursElapsed = ChronoUnit.HOURS.between(req.getSubmittedAt(), LocalDateTime.now());
        map.put("hoursElapsed", hoursElapsed);
        map.put("isUrgent", hoursElapsed > 48);

        return ResponseEntity.ok(map);
    }

    // TÉLÉCHARGER LE PDF
    @GetMapping("/artist-requests/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        try {
            ArtistRequest req = artistRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            if (req.getPdfPath() == null) {
                return ResponseEntity.notFound().build();
            }

            Path pdfPath = Paths.get(req.getPdfPath());
            Resource resource = new UrlResource(pdfPath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // APPROUVER
    @PostMapping("/artist-requests/{id}/approve")
    public ResponseEntity<Map<String, String>> approveRequest(@PathVariable Long id) {
        try {
            authService.approveArtist(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Artiste approuvé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // REJETER
    @PostMapping("/artist-requests/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectRequest(@PathVariable Long id) {
        try {
            authService.rejectArtist(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Demande rejetée");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // STATS DASHBOARD
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long pending = artistRequestRepository.countByStatus(ArtistStatus.PENDING);
        long approved = artistRequestRepository.countByStatus(ArtistStatus.APPROVED);
        long rejected = artistRequestRepository.countByStatus(ArtistStatus.REJECTED);

        // Demandes urgentes (> 48h)
        List<ArtistRequest> pendingList = artistRequestRepository.findByStatus(ArtistStatus.PENDING);
        long urgent = pendingList.stream()
                .filter(req -> ChronoUnit.HOURS.between(req.getSubmittedAt(), LocalDateTime.now()) > 48)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("urgent", urgent);

        return ResponseEntity.ok(stats);
    }
}