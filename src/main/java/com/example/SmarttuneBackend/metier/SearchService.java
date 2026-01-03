package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Artiste> searchArtistes(String query) {
        TypedQuery<Artiste> typedQuery = entityManager.createQuery(
                "SELECT a FROM Artiste a WHERE LOWER(a.nomArtiste) LIKE LOWER(:query)",
                Artiste.class
        );
        typedQuery.setParameter("query", "%" + query + "%");
        return typedQuery.getResultList();
    }

    public List<ChansonResponse> searchChansons(String query) {
        TypedQuery<Chanson> typedQuery = entityManager.createQuery(
                "SELECT c FROM Chanson c WHERE LOWER(c.titre) LIKE LOWER(:query)",
                Chanson.class
        );
        typedQuery.setParameter("query", "%" + query + "%");
        List<Chanson> chansons = typedQuery.getResultList();

        return chansons.stream()
                .map(chanson -> new ChansonResponse(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getUrl(),
                        chanson.getDuree(),
                        chanson.getMusicGenre(),
                        chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                        chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
                ))
                .collect(Collectors.toList());
    }
}