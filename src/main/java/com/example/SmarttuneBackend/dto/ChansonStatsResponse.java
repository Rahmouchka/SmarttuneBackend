package com.example.SmarttuneBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChansonStatsResponse {

    private Long chansonId;
    private String titre;
    private Double moyenneNote;
    private Long nombreEvaluations;

    // Calcul de la note avec un décimal
    public Double getMoyenneFormatee() {
        if (moyenneNote == null) return 0.0;
        return Math.round(moyenneNote * 10.0) / 10.0;
    }
}

