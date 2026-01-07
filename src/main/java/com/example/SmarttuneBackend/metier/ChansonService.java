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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChansonService {

    private final Cloudinary cloudinary;
    private final ChansonRepository chansonRepo;
    private final RestTemplate restTemplate; // Doit être injecté via la config

    private static final String AI_API_URL = "http://localhost:5000/predict";

    @Transactional
    public Chanson uploadChanson(Artiste artiste, MultipartFile file, String titre, MusicGenre genre) {
        // === LIRE LES BYTES UNE SEULE FOIS ===
        byte[] audioBytes;
        try {
            audioBytes = file.getBytes();  // Une seule lecture ici
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier audio", e);
        }

        // 1. Upload sur Cloudinary avec les bytes
        String url;
        try {
            Map result = cloudinary.uploader().upload(audioBytes,
                    ObjectUtils.asMap("resource_type", "auto"));
            url = (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'upload sur Cloudinary", e);
        }

        // 2. Extraction durée (on recrée un faux MultipartFile ou on utilise un temp file)
        String duree = extractDuration(audioBytes, file.getOriginalFilename());

        // 3. Détection humeur avec les mêmes bytes
        String humeur = detectHumeur(audioBytes, file.getOriginalFilename());

        // 4. Création chanson
        Chanson chanson = Chanson.builder()
                .titre(titre)
                .url(url)
                .duree(duree)
                .musicGenre(genre)
                .humeur(humeur)
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

            ResponseEntity<Map> response = restTemplate.postForEntity(AI_API_URL, requestEntity, Map.class);

            System.out.println("Status Flask : " + response.getStatusCode());
            System.out.println("Réponse brute Flask : " + response.getBody());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Réponse Flask non réussie ou corps vide");
                return "inconnue";
            }

            Map<String, Object> json = response.getBody();

            // Cherche l'humeur dans plusieurs clés possibles (au cas où ton Flask change)
            String predictedHumeur = null;
            Double confidence = null;

            for (String key : new String[]{"emotion", "predicted_emotion", "mood", "label", "prediction"}) {
                if (json.containsKey(key) && json.get(key) != null) {
                    predictedHumeur = json.get(key).toString().trim();
                    break;
                }
            }

            // Cherche la confiance
            for (String confKey : new String[]{"confidence", "score", "probability"}) {
                if (json.containsKey(confKey) && json.get(confKey) instanceof Number) {
                    confidence = ((Number) json.get(confKey)).doubleValue();
                    break;
                }
            }

            System.out.println("Humeur extraite : '" + predictedHumeur + "'");
            System.out.println("Confiance : " + confidence);

            // Temporairement : baisse le seuil pour tester si ça passe
            if (predictedHumeur != null && predictedHumeur.length() > 0) {
                if (confidence == null || confidence >= 0.3) {  // seuil bas pour tester !
                    return predictedHumeur;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur appel IA : " + e.getMessage());
            e.printStackTrace();
        }

        return "inconnue";
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