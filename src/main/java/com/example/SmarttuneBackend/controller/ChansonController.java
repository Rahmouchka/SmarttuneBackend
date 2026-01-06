package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.metier.ChansonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chansons")
@RequiredArgsConstructor
public class ChansonController {

    private final ChansonRepository chansonRepository;
    private final ChansonService chansonService;
    // ÉCOUTER UNE CHANSON (retourne détails avec URL pour streaming)
    @GetMapping("/{id}")
    public ResponseEntity<ChansonResponse> getChansonForListen(@PathVariable Long id) {
        Chanson chanson = chansonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + id));

        ChansonResponse response = new ChansonResponse(
                chanson.getId(),
                chanson.getTitre(),
                chanson.getUrl(),
                chanson.getDuree(),
                chanson.getMusicGenre(),
                chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/random")
    public ResponseEntity<List<ChansonResponse>> getRandomChansonsByHumeur(
            @RequestParam String humeur) {

        List<ChansonResponse> chansons = chansonService.getRandomChansonsByHumeur(humeur);

        return ResponseEntity.ok(chansons);
    }
}