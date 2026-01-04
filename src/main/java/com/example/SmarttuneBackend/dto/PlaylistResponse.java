package com.example.SmarttuneBackend.dto;

import java.time.LocalDate;
import java.util.List;

public record PlaylistResponse(
        Long id,
        String titre,
        LocalDate dateCreation,
        boolean visible,
        Long createurId,
        List<ChansonSimple> chansons
) {}