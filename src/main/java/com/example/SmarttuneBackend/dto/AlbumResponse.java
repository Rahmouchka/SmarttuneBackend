package com.example.SmarttuneBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {
    private Long id;
    private String titre;
    private LocalDate dateSortie;
    private String couvertureUrl;
    private List<ChansonSimple> chansons;
}// Utilise ChansonSimple
