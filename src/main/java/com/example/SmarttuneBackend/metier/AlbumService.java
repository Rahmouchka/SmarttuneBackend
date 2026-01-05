package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.AlbumRepository;
import com.example.SmarttuneBackend.dao.ArtisteRepository;
import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String UPLOAD_DIR = "uploads/albums/couvertures/";

    private final AlbumRepository albumRepository;
    private final ChansonRepository chansonRepository;
    private final ArtisteRepository artisteRepository;

    private Path uploadDirectory;

    @PostConstruct
    public void init() {
        uploadDirectory = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier des couvertures d'albums", e);
        }
    }

    // ========================
    // CRÉER UN ALBUM
    // ========================
    public Album createAlbum(Long artisteId, String titre, MultipartFile couverture) {
        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        Album album = Album.builder()
                .titre(titre)
                .artiste(artiste)
                .dateSortie(LocalDate.now())
                .build();

        album = albumRepository.save(album);

        if (couverture != null && !couverture.isEmpty()) {
            String couvertureUrl = saveCouvertureImage(couverture, album.getId());
            album.setCouvertureUrl(couvertureUrl);
            albumRepository.save(album);
        }

        return album;
    }

    // ========================
    // AJOUTER DES CHANSONS À L'ALBUM
    // ========================
    public void addChansonsToAlbum(Long artisteId, Long albumId, List<Long> chansonIds) {
        Album album = albumRepository.findByIdAndArtisteId(albumId, artisteId)
                .orElseThrow(() -> new RuntimeException("Album non trouvé ou ne vous appartient pas"));

        for (Long chansonId : chansonIds) {
            Chanson chanson = chansonRepository.findById(chansonId)
                    .orElseThrow(() -> new RuntimeException("Chanson non trouvée : " + chansonId));

            if (!chanson.getArtiste().getId().equals(artisteId)) {
                throw new RuntimeException("Cette chanson ne vous appartient pas");
            }

            if (chanson.getAlbum() != null && !chanson.getAlbum().getId().equals(albumId)) {
                throw new RuntimeException("La chanson '" + chanson.getTitre() + "' est déjà dans un autre album");
            }

            chanson.setAlbum(album);
            chansonRepository.save(chanson);
        }
    }

    // ========================
    // RETIRER UNE CHANSON DE L'ALBUM
    // ========================
    public Album removeChansonFromAlbum(Long artisteId, Long albumId, Long chansonId) {
        Album album = albumRepository.findByIdAndArtisteId(albumId, artisteId)
                .orElseThrow(() -> new RuntimeException("Album non trouvé ou ne vous appartient pas"));

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée"));

        if (!chanson.getArtiste().getId().equals(artisteId)) {
            throw new RuntimeException("Cette chanson ne vous appartient pas");
        }

        if (chanson.getAlbum() == null || !chanson.getAlbum().getId().equals(albumId)) {
            throw new RuntimeException("Cette chanson n'est pas dans cet album");
        }

        chanson.setAlbum(null);
        chansonRepository.save(chanson);

        return album;
    }

    // ========================
    // LISTE DES ALBUMS - CORRIGÉE
    // ========================
    public List<AlbumResponse> getAlbumsByArtiste(Long artisteId) {
        List<Album> albums = albumRepository.findByArtisteIdOrderByDateSortieDesc(artisteId);

        return albums.stream()
                .map(album -> {
                    // Convertir les chansons en ChansonSimple
                    List<ChansonSimple> chansonsDto = album.getChansons().stream()
                            .map(c -> new ChansonSimple(
                                    c.getId(),
                                    c.getTitre(),
                                    c.getUrl(),
                                    c.getDuree(),
                                    c.getMusicGenre()
                            ))
                            .collect(Collectors.toList());

                    // AlbumResponse avec SEULEMENT 5 paramètres
                    return new AlbumResponse(
                            album.getId(),              // 1
                            album.getTitre(),           // 2
                            album.getDateSortie(),      // 3
                            album.getCouvertureUrl(),   // 4
                            chansonsDto                 // 5
                    );
                })
                .collect(Collectors.toList());
    }

    // ========================
    // SUPPRIMER UN ALBUM
    // ========================
    public void deleteAlbum(Long artisteId, Long albumId) {
        Album album = albumRepository.findByIdAndArtisteId(albumId, artisteId)
                .orElseThrow(() -> new RuntimeException("Album non trouvé ou ne vous appartient pas"));

        if (album.getCouvertureUrl() != null) {
            try {
                Files.deleteIfExists(Paths.get(album.getCouvertureUrl()));
            } catch (IOException e) {
                System.err.println("Erreur suppression couverture : " + e.getMessage());
            }
        }

        albumRepository.delete(album);
    }

    // ========================
    // SAUVEGARDE IMAGE COUVERTURE
    // ========================
    private String saveCouvertureImage(MultipartFile file, Long albumId) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String fileName = albumId + "_" + UUID.randomUUID() + extension;
            Path filePath = uploadDirectory.resolve(fileName);
            Files.write(filePath, file.getBytes());

            return "/uploads/albums/couvertures/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur upload couverture", e);
        }
    }
}