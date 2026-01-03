package com.example.SmarttuneBackend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "favoris",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chanson_id"}))
public class Favoris {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "favoris_chanson",
            joinColumns = @JoinColumn(name = "favoris_id"),
            inverseJoinColumns = @JoinColumn(name = "chanson_id")
    )
    private List<Chanson> chansons = new ArrayList<>();
}