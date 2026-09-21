package com.app.nidai.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.Verse;

public interface VerseRepository extends JpaRepository<Verse, Long> {
    // Traduction SQL générée : SELECT * FROM verse WHERE learning_index <= ? ORDER BY learning_index ASC
    List<Verse> findByLearningIndexLessThanEqualOrderByLearningIndexAsc(Integer learningIndex);
    
    // Trouve un vers spécifique selon son index exact
    Optional<Verse> findByLearningIndex(Integer learningIndex);
}
