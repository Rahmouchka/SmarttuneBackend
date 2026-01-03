package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.dao.AlbumRepository;
import com.example.SmarttuneBackend.entities.Chanson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ChansonRepository chansonRepository;

    public Album createAlbum(Artiste artiste, String titre) {
        Album album = Album.builder()
                .titre(titre)
                .artiste(artiste)
                .build();

        return albumRepository.save(album);
    }
    @Transactional
    public Album addChansonsToAlbum(Artiste artiste, Long albumId, List<Long> chansonIds) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album non trouvé"));

        // Sécurité : l'album doit appartenir à l'artiste
        if (!album.getArtiste().getId().equals(artiste.getId())) {
            throw new RuntimeException("Cet album ne t'appartient pas");
        }

        for (Long chansonId : chansonIds) {
            Chanson chanson = chansonRepository.findById(chansonId)
                    .orElseThrow(() -> new RuntimeException("Chanson non trouvée : " + chansonId));

            // Sécurité : la chanson doit aussi appartenir à l'artiste
            if (!chanson.getArtiste().getId().equals(artiste.getId())) {
                throw new RuntimeException("Tu n'es pas propriétaire de la chanson ID: " + chansonId);
            }

            // Empêche d'ajouter une chanson déjà dans un autre album
            if (chanson.getAlbum() != null && !chanson.getAlbum().getId().equals(albumId)) {
                throw new RuntimeException("La chanson '" + chanson.getTitre() + "' est déjà dans un autre album");
            }

            chanson.setAlbum(album);
            // Pas besoin de save() ici si cascade est bien configuré, mais c'est plus sûr
            chansonRepository.save(chanson);
        }

        // Retourne l'album mis à jour (avec les chansons ajoutées)
        return albumRepository.findById(albumId).get();
    }
    public List<AlbumResponse> getAlbumsByArtiste(Long artisteId) {
        List<Album> albums = albumRepository.findByArtisteIdOrderByDateSortieDesc(artisteId);

        return albums.stream()
                .map(album -> new AlbumResponse(
                        album.getId(),
                        album.getTitre(),
                        album.getDateSortie(),
                        album.getArtiste().getId(),
                        album.getArtiste().getNomArtiste(),
                        album.getChansons().stream()
                                .map(c -> new ChansonSimple(
                                        c.getId(),
                                        c.getTitre(),
                                        c.getUrl(),
                                        c.getMusicGenre()
                                ))
                                .toList()
                ))
                .toList();
    }

    public Album getAlbumWithChansons(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album non trouvé"));
    }

    public void deleteAlbum(Artiste artiste, Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album non trouvé"));

        if (!album.getArtiste().getId().equals(artiste.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        albumRepository.delete(album);
    }
}