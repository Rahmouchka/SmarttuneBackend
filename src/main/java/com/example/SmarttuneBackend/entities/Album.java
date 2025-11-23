package com.example.SmarttuneBackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    private LocalDate dateSortie = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artiste_id", nullable = false)
    @JsonIgnore
    private Artiste artiste;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chanson> chansons = new ArrayList<>();
}