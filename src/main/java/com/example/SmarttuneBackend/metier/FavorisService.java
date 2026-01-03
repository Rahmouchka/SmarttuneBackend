package com.example.SmarttuneBackend.metier;

import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dao.FavorisRepository;
import com.example.SmarttuneBackend.dao.UserRepository;
import com.example.SmarttuneBackend.dto.ChansonSimple;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.Favoris;
import com.example.SmarttuneBackend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavorisService {

    private final FavorisRepository favorisRepository;
    private final ChansonRepository chansonRepository;
    private final UserRepository userRepository;

    private Favoris getOrCreateFavoris(User user) {
        return favorisRepository.findByUser(user)
                .orElseGet(() -> {
                    Favoris newFavoris = new Favoris();
                    newFavoris.setUser(user);
                    newFavoris.setChansons(new ArrayList<>());
                    return favorisRepository.save(newFavoris);
                });
    }

    public void addChansonToFavoris(User user, Long chansonId) {
        Favoris favoris = getOrCreateFavoris(user);
        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + chansonId));

        if (!favoris.getChansons().contains(chanson)) {
            favoris.getChansons().add(chanson);
            favorisRepository.save(favoris);
        }
    }

    public void removeChansonFromFavoris(User user, Long chansonId) {
        Favoris favoris = getOrCreateFavoris(user);
        Chanson chanson = chansonRepository.findById(chansonId)
                .orElseThrow(() -> new RuntimeException("Chanson non trouvée ID: " + chansonId));

        favoris.getChansons().remove(chanson);
        favorisRepository.save(favoris);
    }

    public List<ChansonSimple> getFavorisByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ID: " + userId));

        Favoris favoris = getOrCreateFavoris(user);

        return favoris.getChansons().stream()
                .map(c -> new ChansonSimple(
                        c.getId(),
                        c.getTitre(),
                        c.getUrl(),
                        c.getMusicGenre()
                ))
                .collect(Collectors.toList());
    }
}