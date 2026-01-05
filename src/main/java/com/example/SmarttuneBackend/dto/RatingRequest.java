package com.example.SmarttuneBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    private Integer note; // 1 à 5

    // Validation sera faite dans le service
    public boolean isValid() {
        return note != null && note >= 1 && note <= 5;
    }
}

