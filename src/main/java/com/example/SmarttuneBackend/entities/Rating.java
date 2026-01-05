package com.example.SmarttuneBackend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chanson_id"})) // un seul rate par user/chanson
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chanson_id", nullable = false)
    private Chanson chanson;

    @Column(nullable = false)
    private Integer note; // 1 à 5

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @UpdateTimestamp
    @Column
    private LocalDateTime dateModification = LocalDateTime.now();


}