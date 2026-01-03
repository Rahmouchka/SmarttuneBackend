package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.ArtisteRepository;
import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.*;
import com.example.SmarttuneBackend.metier.AlbumService;
import com.example.SmarttuneBackend.metier.ChansonService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artiste")
@RequiredArgsConstructor
public class ArtisteController {

    private final ChansonService chansonService;
    private final AlbumService albumService;
    private final ArtisteRepository artisteRepository;

    // ========================
    // UPLOAD D'UNE CHANSON
    // ========================
    @PostMapping(value = "/{artisteId}/chansons")
    public ResponseEntity<Chanson> uploadChanson(
            @PathVariable Long artisteId,
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("titre") @NotBlank String titre,
            @RequestParam(value = "musicGenre", required = false) MusicGenre musicGenre)
    {  // Nouveau champ
        System.out.println("Fichier reçu : " + file.getOriginalFilename());
        System.out.println("Taille : " + file.getSize() + " bytes");
        System.out.println("Est vide ? " + file.isEmpty());

        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier uploadé est vide !");
        }
        Artiste artiste = getArtisteOrThrow(artisteId);

        Chanson chanson = chansonService.uploadChanson(artiste, file, titre, musicGenre);
        return ResponseEntity.ok(chanson);
    }

    // ========================
    // CRÉATION D'UN ALBUM (avec couverture optionnelle)
    // ========================
    @PostMapping(value = "/{artisteId}/albums", consumes = "multipart/form-data")
    public ResponseEntity<Album> createAlbum(
            @PathVariable Long artisteId,
            @RequestParam("titre") @NotBlank String titre,
            @RequestParam(value = "couverture", required = false) MultipartFile couverture) {  // Nouveau : photo de couverture

        Artiste artiste = getArtisteOrThrow(artisteId);

        Album album = albumService.createAlbum(artiste.getId(), titre, couverture);
        return ResponseEntity.ok(album);
    }

    // ========================
    // AJOUT DE CHANSONS À UN ALBUM
    // ========================
    @PutMapping("/{artisteId}/albums/{albumId}/chansons")
    public ResponseEntity<String> addChansonsToAlbum(
            @PathVariable Long artisteId,
            @PathVariable Long albumId,
            @RequestBody List<Long> chansonIds) {

        getArtisteOrThrow(artisteId);

        albumService.addChansonsToAlbum(artisteId, albumId, chansonIds);
        return ResponseEntity.ok("Chansons ajoutées à l'album avec succès");
    }

    // ========================
    // SUPPRESSION D'UNE CHANSON
    // ========================
    @DeleteMapping("/{artisteId}/chansons/{chansonId}")
    public ResponseEntity<String> deleteChanson(
            @PathVariable Long artisteId,
            @PathVariable Long chansonId) {

        getArtisteOrThrow(artisteId);

        chansonService.deleteChanson(artisteId, chansonId);
        return ResponseEntity.ok("Chanson supprimée avec succès");
    }

    // ========================
    // RÉCUPÉRER MES ALBUMS
    // ========================
    @GetMapping("/{artisteId}/albums")
    public ResponseEntity<List<AlbumResponse>> getMyAlbums(@PathVariable Long artisteId) {
        getArtisteOrThrow(artisteId);

        List<AlbumResponse> albums = albumService.getAlbumsByArtiste(artisteId);
        return ResponseEntity.ok(albums);
    }

    // ========================
    // RÉCUPÉRER MES CHANSONS
    // ========================
    @GetMapping("/{artisteId}/chansons")
    public ResponseEntity<List<ChansonResponse>> getMySongs(@PathVariable Long artisteId) {
        getArtisteOrThrow(artisteId);

        List<ChansonResponse> chansons = chansonService.getChansonsByArtiste(artisteId);
        return ResponseEntity.ok(chansons);
    }

    // ========================
    // SUPPRESSION D'UN ALBUM
    // ========================
    @DeleteMapping("/{artisteId}/albums/{albumId}")
    public ResponseEntity<String> deleteAlbum(
            @PathVariable Long artisteId,
            @PathVariable Long albumId) {

        getArtisteOrThrow(artisteId);

        albumService.deleteAlbum(artisteId, albumId);
        return ResponseEntity.ok("Album supprimé avec succès");
    }

    // ========================
    // MÉTHODE UTILITAIRE PRIVÉE (réduction duplication)
    // ========================
    private Artiste getArtisteOrThrow(Long artisteId) {
        return artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé avec l'ID : " + artisteId));
    }
    // ========================
    // RETIRER UNE CHANSON D'UN ALBUM
    // ========================
    @DeleteMapping("/{artisteId}/albums/{albumId}/chansons/{chansonId}")
    public ResponseEntity<Album> removeChansonFromAlbum(
            @PathVariable Long artisteId,
            @PathVariable Long albumId,
            @PathVariable Long chansonId) {

        getArtisteOrThrow(artisteId);

        Album albumMisAJour = albumService.removeChansonFromAlbum(artisteId, albumId, chansonId);
        return ResponseEntity.ok(albumMisAJour);
    }
}