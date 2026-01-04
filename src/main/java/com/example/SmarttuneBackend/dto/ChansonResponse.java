package com.example.SmarttuneBackend.dto;

import com.example.SmarttuneBackend.entities.MusicGenre;

public record ChansonResponse(
        Long id,
        String titre,
        String url,
        String duree,
        MusicGenre musicGenre,
        Long albumId,                   // peut être null
        String albumTitre      // null si pas dans un album
) {}