package com.example.SmarttuneBackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString
@Table(name = "chansons")
public class Chanson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String url;

    @Column
    private String humeur;

    @Column(nullable = false)
    private Integer signalements = 0;

    private String duree;
    private LocalDate dateSortie = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private MusicGenre musicGenre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(mappedBy = "chansons")
    private List<Playlist> playlists = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artiste_id", nullable = false)
    @JsonIgnore
    private Artiste artiste;
}