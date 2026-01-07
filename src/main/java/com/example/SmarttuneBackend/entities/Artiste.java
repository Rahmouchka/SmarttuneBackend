package com.example.SmarttuneBackend.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ARTIST")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Artiste extends User {

    private String nomArtiste;
    private Integer nbrAbonnees = 0;

    @OneToMany(mappedBy = "artiste", cascade = CascadeType.ALL, orphanRemoval = true)    private List<Album> albums = new ArrayList<>();

}
