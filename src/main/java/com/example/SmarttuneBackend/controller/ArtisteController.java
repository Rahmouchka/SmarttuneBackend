package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.ArtisteRepository;
import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.MusicGenre;
import com.example.SmarttuneBackend.metier.AlbumService;
import com.example.SmarttuneBackend.metier.ChansonService;
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

    // UPLOAD CHANSON
    @PostMapping("/{artisteId}/chansons")
    public ResponseEntity<Chanson> uploadChanson(
            @PathVariable Long artisteId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam(value = "musicGenre", required = false) MusicGenre musicGenre) {

        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé ID: " + artisteId));

        Chanson chanson = chansonService.uploadChanson(artiste, file, titre, musicGenre);
        return ResponseEntity.ok(chanson);
    }

    @PostMapping("/{artisteId}/albums")
    public ResponseEntity<Album> createAlbum(
            @PathVariable Long artisteId,
            @RequestParam("titre") String titre) {

        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        Album album = albumService.createAlbum(artiste, titre);
        return ResponseEntity.ok(album);
    }

    @PutMapping("/{artisteId}/albums/{albumId}/chansons")
    public ResponseEntity<Album> addChansonsToAlbum(
            @PathVariable Long artisteId,
            @PathVariable Long albumId,
            @RequestBody List<Long> chansonIds) {

        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        Album albumMisAJour = albumService.addChansonsToAlbum(artiste, albumId, chansonIds);
        return ResponseEntity.ok(albumMisAJour);
    }

    @DeleteMapping("/{artisteId}/chansons/{id}")
    public ResponseEntity<String> deleteChanson(
            @PathVariable Long artisteId,
            @PathVariable Long id) {

        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        chansonService.deleteChanson(artiste, id);
        return ResponseEntity.ok("Chanson supprimée avec succès");
    }

    @GetMapping("/{artisteId}/albums")
    public ResponseEntity<List<AlbumResponse>> getMyAlbums(@PathVariable Long artisteId) {
        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        return ResponseEntity.ok(albumService.getAlbumsByArtiste(artisteId));
    }
    @GetMapping("/{artisteId}/chansons")
    public ResponseEntity<List<ChansonResponse>> getMySongs(@PathVariable Long artisteId) {
        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé ID: " + artisteId));

        List<ChansonResponse> chansons = chansonService.getChansonsByArtiste(artisteId);
        return ResponseEntity.ok(chansons);
    }
    @DeleteMapping("/{artisteId}/albums/{id}")
    public ResponseEntity<String> deleteAlbum(
            @PathVariable Long artisteId,
            @PathVariable Long id) {

        Artiste artiste = artisteRepository.findById(artisteId)
                .orElseThrow(() -> new RuntimeException("Artiste non trouvé"));

        albumService.deleteAlbum(artiste, id);
        return ResponseEntity.ok("Album supprimé avec succès");
    }
}