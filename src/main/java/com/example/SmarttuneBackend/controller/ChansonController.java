package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Chanson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chansons")
@RequiredArgsConstructor
public class ChansonController {

    private final ChansonRepository chansonRepository;

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
}