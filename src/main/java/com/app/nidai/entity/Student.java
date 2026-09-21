package com.app.nidai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "student")
@Getter @Setter @NoArgsConstructor 
public class Student {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_id", nullable = false, unique = true, length = 10)
    private String shortId;

    @Column(name = "current_palier", nullable = false)
    private Integer currentPalier = 0;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudentGroup group;

    @ManyToOne
    @JoinColumn(name = "last_validated_verse_id")
    private Verse lastValidatedVerse;
}
