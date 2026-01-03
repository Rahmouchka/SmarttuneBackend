package com.example.SmarttuneBackend.dto;
import java.time.LocalDate;
import java.util.List;

public record AlbumResponse(
        Long id,
        String titre,
        LocalDate dateSortie,
        String couvertureUrl,           // nouveau champ
        Long artisteId,
        String nomArtiste,
        List<ChansonSimple> chansons
) {}