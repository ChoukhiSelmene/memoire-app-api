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
@Table(name = "verse")
@Getter 
@Setter 
@NoArgsConstructor  
public class Verse {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "verse_number", nullable = false)
    private Integer verseNumber;

    // L'ordre absolu d'apprentissage (1 à N) pour le système de curseur
    @Column(name = "learning_index", nullable = false, unique = true)
    private Integer learningIndex;

    @Column(name = "is_end_of_chapter", nullable = false)
    private Boolean isEndOfChapter = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(optional = false)
    @JoinColumn (name = "poem_id", nullable = false)
    private Poem poem;
}
