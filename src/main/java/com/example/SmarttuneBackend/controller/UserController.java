package com.example.SmarttuneBackend.controller;

import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.PlaylistResponse;
import com.example.SmarttuneBackend.entities.User;
import com.example.SmarttuneBackend.metier.FavorisService;
import com.example.SmarttuneBackend.metier.PlaylistService;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final PlaylistService playlistService;
    private final FavorisService favorisService;
    private final UserRepository userRepository;

    // GESTION PLAYLISTS

    @PostMapping("/{userId}/playlists")
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @PathVariable Long userId,
            @RequestParam("titre") String titre,
            @RequestParam(value = "visible", defaultValue = "true") boolean visible) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(playlistService.createPlaylist(user, titre, visible));
    }

    @GetMapping("/{userId}/playlists")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(playlistService.getPlaylistsByUser(userId));
    }

    @PutMapping("/{userId}/playlists/{playlistId}")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @RequestParam("titre") String titre,
            @RequestParam("visible") boolean visible) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(playlistService.updatePlaylist(user, playlistId, titre, visible));
    }

    @PostMapping("/{userId}/playlists/{playlistId}/chansons")
    public ResponseEntity<PlaylistResponse> addChansonsToPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @RequestBody List<Long> chansonIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(playlistService.addChansonsToPlaylist(user, playlistId, chansonIds));
    }

    @DeleteMapping("/{userId}/playlists/{playlistId}/chansons/{chansonId}")
    public ResponseEntity<PlaylistResponse> removeChansonFromPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @PathVariable Long chansonId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(playlistService.removeChansonFromPlaylist(user, playlistId, chansonId));
    }

    @DeleteMapping("/{userId}/playlists/{playlistId}")
    public ResponseEntity<String> deletePlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        playlistService.deletePlaylist(user, playlistId);
        return ResponseEntity.ok("Playlist supprimée avec succès");
    }

    // GESTION FAVORIS

    @GetMapping("/{userId}/favoris")
    public ResponseEntity<List<ChansonSimple>> getMyFavoris(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        return ResponseEntity.ok(favorisService.getFavorisByUser(userId));
    }

    @PostMapping("/{userId}/favoris")
    public ResponseEntity<String> addToFavoris(
            @PathVariable Long userId,
            @RequestParam("chansonId") Long chansonId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        favorisService.addChansonToFavoris(user, chansonId);
        return ResponseEntity.ok("Chanson ajoutée aux favoris");
    }

    @DeleteMapping("/{userId}/favoris/{chansonId}")
    public ResponseEntity<String> removeFromFavoris(
            @PathVariable Long userId,
            @PathVariable Long chansonId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        favorisService.removeChansonFromFavoris(user, chansonId);
        return ResponseEntity.ok("Chanson supprimée des favoris");
    }
}