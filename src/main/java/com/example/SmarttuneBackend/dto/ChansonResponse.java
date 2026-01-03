package com.example.SmarttuneBackend.dto;

import com.example.SmarttuneBackend.entities.MusicGenre;

public record ChansonResponse(
        Long id,
        String titre,
        String url,
        MusicGenre musicGenre,
        Long albumId,           // null si pas dans un album
        String albumTitre       // null si pas dans un album
) {}