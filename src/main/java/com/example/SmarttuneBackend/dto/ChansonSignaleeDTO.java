package com.example.SmarttuneBackend.dto;

// ChansonSignaleeDTO.java
public record ChansonSignaleeDTO(
        Long id,
        String titre,
        String nomArtiste,  // ← seulement ça
        Integer signalements,
        String musicGenre,
        String duree
) {}