package com.app.nidai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "course_session")
@Getter @Setter @NoArgsConstructor
public class CourseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate sessionDate;

    // Optionnel : un titre pour la séance, ex: "Séance du mardi - Groupe A"
    @Column(nullable = false)
    private String title;
}