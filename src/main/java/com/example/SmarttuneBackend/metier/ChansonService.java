package com.example.SmarttuneBackend.metier;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.dto.ChansonSignaleeDTO;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.MusicGenre;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChansonService {

    private final Cloudinary cloudinary;
    private final ChansonRepository chansonRepo;
    private final RestTemplate restTemplate;

    private static final String AI_API_URL = "http://127.0.0.1:8000/predict";

    @Transactional
    public Chanson uploadChanson(Artiste artiste, MultipartFile file, String titre, MusicGenre genre) {
        // 1. Validations de base
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier audio requis");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        // Validation extension (optionnel mais fortement recommandé)
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        Set<String> allowed = Set.of("mp3", "wav", "m4a", "ogg", "flac");
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException("Format audio non supporté : " + extension);
        }

        // 2. Limite de taille (exemple : 50MB)
        long maxSizeBytes = 50 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 50MB)");
        }

        byte[] audioBytes;
        try {
            audioBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier audio", e);
        }

        // 3. Upload Cloudinary
        String url;
        try {
            Map result = cloudinary.uploader().upload(audioBytes,
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "chansons",                // organisation
                            "public_id", UUID.randomUUID().toString() // évite collisions
                    ));
            url = (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Échec upload Cloudinary", e);
        }

        // 4. Extraction des métadonnées
        String duree = extractDuration(audioBytes, originalFilename);

        // 5. Détection humeur (peut être lente → potentiellement async plus tard)
        String humeur = detectHumeur(audioBytes, originalFilename);

        // 6. Création entité
        Chanson chanson = Chanson.builder()
                .titre(StringUtils.trimToNull(titre))  // nettoyage
                .url(url)
                .duree(duree)
                .musicGenre(genre)
                .humeur(humeur != null ? humeur.toLowerCase() : "inconnue")
                .dateSortie(LocalDate.now())
                .signalements(0)
                .artiste(artiste)
                .album(null)
                .playlists(new ArrayList<>())
                .ratings(new ArrayList<>())
                .build();

        return chansonRepo.save(chanson);
    }
    private String extractDuration(byte[] audioBytes, String originalFilename) {
        String duree = "00:00";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("duration_", ".mp3");
            Files.write(tempFile.toPath(), audioBytes);

            Mp3File mp3file = new Mp3File(tempFile);
            long lengthInSeconds = mp3file.getLengthInSeconds();
            int minutes = (int) (lengthInSeconds / 60);
            int seconds = (int) (lengthInSeconds % 60);
            duree = String.format("%02d:%02d", minutes, seconds);

        } catch (Exception e) {
            System.err.println("Erreur extraction durée : " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return duree;
    }
    private String detectHumeur(byte[] audioBytes, String originalFilename) {
        try {
            System.out.println("=== Envoi à l'IA Flask : " + originalFilename + " (" + audioBytes.length + " bytes) ===");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    AI_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            System.out.println("Status : " + response.getStatusCode());

            Map<String, Object> json = response.getBody();
            if (json == null) {
                System.err.println("Réponse vide du Flask");
                return "inconnue";
            }

            System.out.println("Réponse Flask complète : " + json);

            String predictedHumeur = (String) json.get("predicted_mood");
            Double confidence = null;
            if (json.get("confidence") instanceof Number) {
                confidence = ((Number) json.get("confidence")).doubleValue();
            }

            System.out.println("Mood détecté : " + predictedHumeur + " (confiance: " + confidence + ")");

            if (predictedHumeur != null && !predictedHumeur.isEmpty()) {
                if (confidence == null || confidence >= 0.3) {
                    return predictedHumeur.toLowerCase();
                }
            }

            return "inconnue";

        } catch (Exception e) {
            System.err.println("Erreur appel Flask : " + e.getMessage());
            e.printStackTrace();
            return "inconnue";
        }
    }
    // === Autres méthodes (tu peux les garder telles quelles) ===
    public List<ChansonResponse> getChansonsByArtiste(Long artisteId) {
        // ton code existant
        return chansonRepo.findByArtisteId(artisteId).stream()
                .map(chanson -> new ChansonResponse(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getUrl(),
                        chanson.getDuree(),
                        chanson.getMusicGenre(),
                        chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                        chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
                ))
                .toList();
    }

    @Transactional
    public void deleteChanson(Long artisteId, Long chansonId) {
        Chanson chanson = chansonRepo.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée"));

        if (!chanson.getArtiste().getId().equals(artisteId)) {
            throw new RuntimeException("Tu n'es pas propriétaire de cette chanson");
        }

        // Suppression sur Cloudinary
        try {
            String publicId = extractPublicIdFromUrl(chanson.getUrl());
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
            }
        } catch (Exception e) {
            System.err.println("Erreur suppression Cloudinary : " + e.getMessage());
        }

        chansonRepo.delete(chanson);
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) return null;
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf(".");
        return (end > start) ? url.substring(start, end) : null;
    }
    public List<ChansonResponse> getRandomChansonsByHumeur(String humeur) {
        if (humeur == null || humeur.trim().isEmpty()) {
            throw new IllegalArgumentException("L'humeur ne peut pas être vide");
        }

        Pageable pageable = PageRequest.of(0, 10);
        List<Chanson> chansons = chansonRepo.findRandomByHumeur(humeur.toLowerCase(), pageable);

        return chansons.stream()
                .map(chanson -> new ChansonResponse(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getUrl(),
                        chanson.getDuree(),
                        chanson.getMusicGenre(),
                        chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                        chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
                ))
                .toList();
    }
    @Transactional
    public void signalerChanson(Long chansonId) {
        Chanson chanson = chansonRepo.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + chansonId));

        chanson.setSignalements(chanson.getSignalements() + 1);

        // save facultatif car transactionnel, mais plus explicite
        chansonRepo.save(chanson);
    }
    @Transactional(readOnly = true)
    public List<ChansonSignaleeDTO> getChansonsSignalees() {
        return chansonRepo.findReportedChansonsOrderedBySignalements()
                .stream()
                .map(chanson -> new ChansonSignaleeDTO(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getArtiste() != null ? chanson.getArtiste().getNomArtiste() : "Artiste inconnu",
                        chanson.getSignalements(),
                        chanson.getMusicGenre() != null ? chanson.getMusicGenre().name() : null,
                        chanson.getDuree()
                ))
                .toList();
    }
}