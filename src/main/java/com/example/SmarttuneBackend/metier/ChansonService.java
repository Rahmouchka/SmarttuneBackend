package com.example.SmarttuneBackend.metier;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SmarttuneBackend.dao.AlbumRepository;
import com.example.SmarttuneBackend.dao.ArtisteRepository;
import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.MusicGenre;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChansonService {

    private final Cloudinary cloudinary;
    private final ChansonRepository chansonRepo;
    private final AlbumRepository albumRepo;
    private final ArtisteRepository artisteRepo;

    // ========================
    // UPLOAD D'UNE CHANSON
    // ========================
    @Transactional
    public Chanson uploadChanson(Artiste artiste, MultipartFile file, String titre, MusicGenre genre) {
        // Upload sur Cloudinary
        String url;
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            url = (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'upload sur Cloudinary", e);
        }

        String duree = "00:00";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // Lire les métadonnées
            Mp3File mp3file = new Mp3File(tempFile);
            long lengthInSeconds = mp3file.getLengthInSeconds();

            int minutes = (int) (lengthInSeconds / 60);
            int seconds = (int) (lengthInSeconds % 60);
            duree = String.format("%02d:%02d", minutes, seconds);

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            System.err.println("Impossible d'extraire la durée du fichier : " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        Chanson chanson = Chanson.builder()
                .titre(titre)
                .url(url)
                .musicGenre(genre)
                .duree(duree)
                .dateSortie(LocalDate.now())
                .signalements(0)
                .artiste(artiste)
                .album(null)
                .build();

        return chansonRepo.save(chanson);
    }
    // ========================
    // RÉCUPÉRER TOUTES LES CHANSONS
    // ========================
    public List<Chanson> findAll() {
        return chansonRepo.findAll();
    }

    public Chanson findById(Long id) {
        return chansonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée"));
    }

    public List<Chanson> findByArtisteId(Long artisteId) {
        return chansonRepo.findByArtisteId(artisteId);
    }

    // ========================
    // DTO POUR L'ARTISTE (ses chansons)
    // ========================
    public List<ChansonResponse> getChansonsByArtiste(Long artisteId) {
        List<Chanson> chansons = chansonRepo.findByArtisteId(artisteId);

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

    // ========================
    // SUPPRIMER UNE CHANSON
    // ========================
    @Transactional
    public void deleteChanson(Long artisteId, Long chansonId) {  // ← corrigé : Long artisteId
        Chanson chanson = chansonRepo.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée"));

        if (!chanson.getArtiste().getId().equals(artisteId)) {  // ← comparaison directe avec Long
            throw new RuntimeException("Tu n'es pas propriétaire de cette chanson");
        }

        try {
            String publicId = extractPublicIdFromUrl(chanson.getUrl());
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression sur Cloudinary : " + e.getMessage());
            // On ne bloque pas la suppression locale même si Cloudinary échoue
        }

        chansonRepo.delete(chanson);
    }

    // ========================
    // UTILITAIRE : extraire le public_id de l'URL Cloudinary
    // ========================
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) return null;
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf(".");
        if (end > start) {
            return url.substring(start, end);
        }
        return null;
    }
}