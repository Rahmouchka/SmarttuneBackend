package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dao.PlaylistRepository;
import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import com.example.SmarttuneBackend.dto.PlaylistResponse;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.Playlist;
import com.example.SmarttuneBackend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final ChansonRepository chansonRepository;
    private final UserRepository userRepository;

    public PlaylistResponse createPlaylist(User user, String titre, boolean visible) {
        Playlist playlist = new Playlist();
        playlist.setTitre(titre);
        playlist.setVisible(visible);
        playlist.setCreateur(user);
        playlist.setChansons(new ArrayList<>());
        playlist = playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public PlaylistResponse updatePlaylist(User user, Long playlistId, String titre, boolean visible) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée ID: " + playlistId));

        if (!playlist.getCreateur().getId().equals(user.getId())) {
            throw new RuntimeException("Cette playlist ne vous appartient pas");
        }

        playlist.setTitre(titre);
        playlist.setVisible(visible);
        playlist = playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public PlaylistResponse addChansonsToPlaylist(User user, Long playlistId, List<Long> chansonIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée ID: " + playlistId));

        if (!playlist.getCreateur().getId().equals(user.getId())) {
            throw new RuntimeException("Cette playlist ne vous appartient pas");
        }

        for (Long id : chansonIds) {
            Chanson chanson = chansonRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + id));
            if (!playlist.getChansons().contains(chanson)) {
                playlist.getChansons().add(chanson);
            }
        }

        playlist = playlistRepository.save(playlist);
        return mapToResponse(playlist);
    }

    public PlaylistResponse removeChansonFromPlaylist(User user, Long playlistId, Long chansonId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée ID: " + playlistId));

        if (!playlist.getCreateur().getId().equals(user.getId())) {
            throw new RuntimeException("Cette playlist ne vous appartient pas");
        }

        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + chansonId));

        playlist.getChansons().remove(chanson);
        playlist = playlistRepository.save(playlist);

        return mapToResponse(playlist);
    }

    public void deletePlaylist(User user, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist non trouvée ID: " + playlistId));

        if (!playlist.getCreateur().getId().equals(user.getId())) {
            throw new RuntimeException("Cette playlist ne vous appartient pas");
        }

        playlistRepository.delete(playlist);
    }

    public List<PlaylistResponse> getPlaylistsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        List<Playlist> playlists = playlistRepository.findByCreateur(user);

        return playlists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {
        List<ChansonSimple> chansons = playlist.getChansons().stream()
                .map(c -> new ChansonSimple(
                        c.getId(),
                        c.getTitre(),
                        c.getUrl(),
                        c.getDuree(),
                        c.getMusicGenre()
                ))
                .collect(Collectors.toList());

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getTitre(),
                playlist.getDateCreation(),
                playlist.isVisible(),
                playlist.getCreateur().getId(),
                chansons
        );
    }
}