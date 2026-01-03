package com.example.SmarttuneBackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private LocalDate dateCreation = LocalDate.now();
    private boolean visible = true;

    @ManyToOne
    @JoinColumn(name = "createur_id")
    private User createur;

    @ManyToMany
    @JoinTable(
            name = "playlist_chanson",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "chanson_id")
    )
    private List<Chanson> chansons = new ArrayList<>();
}
