package com.example.SmarttuneBackend.dto;

import com.example.SmarttuneBackend.entities.MusicGenre;

public record ChansonSimple(
        Long id,
        String titre,
        String url,
        String duree,
        MusicGenre musicGenre
) {}
