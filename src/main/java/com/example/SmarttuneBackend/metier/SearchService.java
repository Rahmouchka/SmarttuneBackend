package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dto.AlbumResponse;
import com.example.SmarttuneBackend.dto.ArtisteResponse;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Recherche d'artistes - Retourne des DTOs sans les albums/chansons
     */
    public List<ArtisteResponse> searchArtistes(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        String searchTerm = "%" + query.toLowerCase() + "%";

        String jpql = """
        SELECT a FROM Artiste a 
        WHERE LOWER(a.nomArtiste) LIKE :term
           OR LOWER(a.prenom) LIKE :term
           OR LOWER(a.nom) LIKE :term
           OR LOWER(CONCAT(COALESCE(a.prenom,''), ' ', COALESCE(a.nom,''))) LIKE :term
        """;

        TypedQuery<Artiste> typedQuery = entityManager.createQuery(jpql, Artiste.class);
        typedQuery.setParameter("term", searchTerm);

        List<Artiste> artistes = typedQuery.getResultList();

        // Conversion en DTO - SANS albums ni chansons
        return artistes.stream()
                .map(a -> new ArtisteResponse(
                        a.getId(),
                        a.getNomArtiste(),
                        a.getPrenom(),
                        a.getNom(),
                        a.getBio(),
                        a.getEmail(),
                        a.getNbrAbonnees(),
                        a.getNbrAbonnements()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Recherche de chansons - retourne toutes si query vide
     */
    public List<ChansonResponse> searchChansons(String query) {
        String jpql;
        TypedQuery<Chanson> typedQuery;

        if (!StringUtils.hasText(query)) {
            jpql = "SELECT c FROM Chanson c";
            typedQuery = entityManager.createQuery(jpql, Chanson.class);
        } else {
            jpql = "SELECT c FROM Chanson c WHERE LOWER(c.titre) LIKE LOWER(:query)";
            typedQuery = entityManager.createQuery(jpql, Chanson.class);
            typedQuery.setParameter("query", "%" + query + "%");
        }

        List<Chanson> chansons = typedQuery.getResultList();

        return chansons.stream()
                .map(chanson -> new ChansonResponse(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getUrl(),
                        chanson.getDuree(),
                        chanson.getMusicGenre(),  // ← Pas besoin de conversion
                        chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                        chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
                ))
                .collect(Collectors.toList());
    }

    /**
     * Recherche d'albums - Retourne des DTOs avec chansons SIMPLES (sans référence album)
     */
    public List<AlbumResponse> searchAlbums(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        String searchTerm = "%" + query.toLowerCase() + "%";

        TypedQuery<Album> typedQuery = entityManager.createQuery(
                "SELECT al FROM Album al WHERE LOWER(al.titre) LIKE :term",
                Album.class
        );
        typedQuery.setParameter("term", searchTerm);

        List<Album> albums = typedQuery.getResultList();

        // Conversion en DTO avec ChansonSimple (SANS référence à l'album)
        return albums.stream()
                .map(album -> {
                    List<ChansonSimple> chansonsDto = album.getChansons().stream()
                            .map(c -> new ChansonSimple(
                                    c.getId(),
                                    c.getTitre(),
                                    c.getUrl(),
                                    c.getDuree(),
                                    c.getMusicGenre()  // ← Pas besoin de conversion
                            ))
                            .collect(Collectors.toList());

                    return new AlbumResponse(
                            album.getId(),
                            album.getTitre(),
                            album.getDateSortie(),
                            album.getCouvertureUrl(),
                            chansonsDto
                    );
                })
                .collect(Collectors.toList());
    }
}