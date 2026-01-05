package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ArtisteResponse;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.metier.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Recherche d'artistes - Retourne uniquement les infos de l'artiste
     */
    @GetMapping("/artistes")
    public ResponseEntity<List<ArtisteResponse>> searchArtistes(@RequestParam("query") String query) {
        List<ArtisteResponse> artistes = searchService.searchArtistes(query);
        return ResponseEntity.ok(artistes);
    }

    /**
     * Recherche de chansons
     */
    @GetMapping("/chansons")
    public ResponseEntity<List<ChansonResponse>> searchChansons(@RequestParam("query") String query) {
        List<ChansonResponse> chansons = searchService.searchChansons(query);
        return ResponseEntity.ok(chansons);
    }

    /**
     * Recherche d'albums - Retourne albums avec chansons (sans boucle)
     */
    @GetMapping("/albums")
    public ResponseEntity<List<AlbumResponse>> searchAlbums(@RequestParam("query") String query) {
        List<AlbumResponse> albums = searchService.searchAlbums(query);
        return ResponseEntity.ok(albums);
    }
}