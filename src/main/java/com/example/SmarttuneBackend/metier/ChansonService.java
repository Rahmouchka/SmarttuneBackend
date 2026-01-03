package com.example.SmarttuneBackend.metier;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SmarttuneBackend.dao.AlbumRepository;
import com.example.SmarttuneBackend.dao.ArtisteRepository;
import com.example.SmarttuneBackend.dao.ChansonRepository;
import com.example.SmarttuneBackend.dto.ChansonResponse;
import com.example.SmarttuneBackend.entities.Album;
import com.example.SmarttuneBackend.entities.Artiste;
import com.example.SmarttuneBackend.entities.Chanson;
import com.example.SmarttuneBackend.entities.MusicGenre;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChansonService {

    private final Cloudinary cloudinary;
    private final ChansonRepository chansonRepo;
    private final AlbumRepository albumRepo;
    private final ArtisteRepository artisteRepo;

    @Transactional
    public Chanson uploadChanson(Artiste artiste, MultipartFile file, String titre, MusicGenre genre) {
        // Upload Cloudinary
        String url;
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            url = (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Échec upload Cloudinary", e);
        }

        Chanson chanson = Chanson.builder()
                .titre(titre)
                .url(url)
                .musicGenre(genre)
                .artiste(artiste)
                .album(null)  // ← pas d'album au début
                .build();

        return chansonRepo.save(chanson);
    }

    public List<Chanson> findAll() { return chansonRepo.findAll(); }
    public Chanson findById(Long id) { return chansonRepo.findById(id).orElseThrow(); }
    public List<Chanson> findByArtisteId(Long artisteId) {
        return chansonRepo.findByArtisteId(artisteId);
    }
    public List<ChansonResponse> getChansonsByArtiste(Long artisteId) {
        List<Chanson> chansons = chansonRepo.findByArtisteId(artisteId);

        return chansons.stream()
                .map(chanson -> new ChansonResponse(
                        chanson.getId(),
                        chanson.getTitre(),
                        chanson.getUrl(),
                        chanson.getMusicGenre(),
                        chanson.getAlbum() != null ? chanson.getAlbum().getId() : null,
                        chanson.getAlbum() != null ? chanson.getAlbum().getTitre() : null
                ))
                .toList();
    }
    @Transactional
    public void deleteChanson(Artiste artiste, Long chansonId) {
        Chanson chanson = chansonRepo.findById(chansonId).orElseThrow();
        if (!chanson.getArtiste().getId().equals(artiste.getId())) {
            throw new RuntimeException("Tu n'es pas propriétaire de cette chanson");
        }
        chansonRepo.delete(chanson);
    }
}