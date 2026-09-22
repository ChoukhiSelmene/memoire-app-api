package com.app.nidai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.nidai.dto.VerseDto;
import com.app.nidai.entity.Chapter;
import com.app.nidai.entity.Student;
import com.app.nidai.entity.Verse;
import com.app.nidai.repository.AttendanceRepository;
import com.app.nidai.repository.CourseSessionRepository;
import com.app.nidai.repository.StudentRepository;
import com.app.nidai.repository.VerseRepository;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    // 1. On "Mock" (simule) toutes les dépendances requises par le constructeur du Service
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private VerseRepository verseRepository;
    @Mock
    private CourseSessionRepository courseSessionRepository;
    @Mock
    private AttendanceRepository attendanceRepository;

    // 2. On injecte automatiquement ces faux Repositories dans notre vrai Service
    @InjectMocks
    private StudentService studentService;

    @Test 
    void getValidatedVerses_ShouldThrowException_WhenStudentNotFound() {
        // Arrange : On configure le mock pour qu'il retourne un Optional vide
        when(studentRepository.findByShortId("INCONNU")).thenReturn(Optional.empty());

        // Act & Assert : On vérifie que l'appel lève bien la bonne exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> studentService.getValidatedVerses("INCONNU")
        );

        assertEquals("Etudiant introuvable : INCONNU", exception.getMessage());

        // On vérifie que le VerseRepository n'a jamais été sollicité
        verify(verseRepository, never()).findByLearningIndexLessThanEqualOrderByLearningIndexAsc(anyInt());
    }

    @Test 
    void getValidatedVerses_ShouldReturnList_WhenStudentHasCursor() {
        // Arrange : On fabrique nos fausses données
        Student mockStudent = new Student();
        mockStudent.setShortId("MEMO-TEST");

        Chapter mockChapter = new Chapter();
        mockChapter.setNumber(60);

        Verse mockVerse = new Verse();
        mockVerse.setLearningIndex(10);
        mockVerse.setChapter(mockChapter);
        mockVerse.setVerseNumber(1);
        mockVerse.setIsEndOfChapter(false);

        mockStudent.setLastValidatedVerse(mockVerse);

        // On dicte au mock son comportement
        when(studentRepository.findByShortId("MEMO-TEST")).thenReturn(Optional.of(mockStudent));
        when(verseRepository.findByLearningIndexLessThanEqualOrderByLearningIndexAsc(10))
                .thenReturn(List.of(mockVerse));

        // Act : On exécute la méthode métier
        List<VerseDto> result = studentService.getValidatedVerses("MEMO-TEST");
        
        // Assert : On vérifie que le DTO est correctement mappé
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(60, result.get(0).chapterNumber());
        assertEquals(1, result.get(0).verseNumber());
        assertFalse(result.get(0).isEndOfChapter());
    }
}
