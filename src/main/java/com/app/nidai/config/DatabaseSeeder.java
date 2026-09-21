package com.app.nidai.config;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.nidai.entity.Attendance;
import com.app.nidai.entity.AttendanceStatus;
import com.app.nidai.entity.Chapter;
import com.app.nidai.entity.CourseSession;
import com.app.nidai.entity.Poem;
import com.app.nidai.entity.Student;
import com.app.nidai.entity.StudentGroup;
import com.app.nidai.entity.Verse;
import com.app.nidai.repository.AttendanceRepository;
import com.app.nidai.repository.ChapterRepository;
import com.app.nidai.repository.CourseSessionRepository;
import com.app.nidai.repository.PoemRepository;
import com.app.nidai.repository.StudentGroupRepository;
import com.app.nidai.repository.StudentRepository;
import com.app.nidai.repository.VerseRepository;

import jakarta.transaction.Transactional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ChapterRepository chapterRepository;
    private final PoemRepository poemRepository;
    private final VerseRepository verseRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentRepository studentRepository;
    // --- NOUVEAUX REPOSITORIES ---
    private final CourseSessionRepository courseSessionRepository;
    private final AttendanceRepository attendanceRepository;

    public DatabaseSeeder(ChapterRepository chapterRepository, 
                          PoemRepository poemRepository, 
                          VerseRepository verseRepository,
                          StudentGroupRepository studentGroupRepository,
                          StudentRepository studentRepository,
                          CourseSessionRepository courseSessionRepository,
                          AttendanceRepository attendanceRepository) {
        this.chapterRepository = chapterRepository;
        this.poemRepository = poemRepository;
        this.verseRepository = verseRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.studentRepository = studentRepository;
        this.courseSessionRepository = courseSessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional 
    public void run(String... args) throws Exception {
        // On évite de repeupler la base si elle contient déjà des données
        if (verseRepository.count() > 0) {
            return;
        }

        // 1. Création d'un poème fictif pour le POC
        Poem poem = new Poem();
        poem.setTitle("L'Épopée de la Mémoire");
        poemRepository.save(poem);

        int versesPerChapter = 10;
        int totalChapters = 60;

        // 1. IntStream.iterate crée un flux d'entiers de 60 à 1
        List<Verse> versesToSave = 
            IntStream.iterate(totalChapters, chapNum -> chapNum >= 1, chapNum -> chapNum - 1)
            // 2. mapToObj transforme chaque entier en un objet Chapter sauvegardé
            .mapToObj(chapNum -> {
                Chapter chapter = new Chapter();
                chapter.setNumber(chapNum);
                chapter.setTitle("Chapitre " + chapNum);
                return chapterRepository.save(chapter);
            })
            // 3. flatMap "aplatit" les listes de vers générées pour chaque chapitre en un seul grand flux de vers
            .flatMap(chapter -> IntStream.rangeClosed(1, versesPerChapter)
                .mapToObj(verseNum -> {
                    Verse verse = new Verse();
                    verse.setVerseNumber(verseNum);
                    verse.setChapter(chapter);
                    verse.setPoem(poem);

                    // Calcul pur du learningIndex (sans variable externe) : 
                    // Ex: Chapitre 60 -> sequence 0 -> vers 1 à 10. Chapitre 59 -> sequence 1 -> vers 11 à 20.
                    int sequence = totalChapters - chapter.getNumber();
                    verse.setLearningIndex((sequence * versesPerChapter) + verseNum);

                    verse.setIsEndOfChapter(verseNum == versesPerChapter);
                    return verse;
                })
            )
            // 4. toList() terminalise le Stream et collecte les résultats
            .toList();

        // 5. Sauvegarde en batch, beaucoup plus performant qu'un save() à chaque itération
        verseRepository.saveAll(versesToSave);

        if(studentRepository.count() == 0) {
            StudentGroup group = new StudentGroup();
            group.setName("Les Pionniers");
            group.setTargetPalier(2);
            studentGroupRepository.save(group);

            Student student = new Student();
            student.setShortId("MEMO-TEST");
            student.setGroup(group);
            student.setCurrentPalier(0);

            // Placement du curseur au 15ème vers global (5ème vers du chapitre 59)
            verseRepository.findById(15L).ifPresent(student::setLastValidatedVerse);

            studentRepository.save(student);

        // --- SEEDING DU CALENDRIER ET DES PRESENCES ---
            CourseSession session = new CourseSession();
            session.setSessionDate(java.time.LocalDate.now());
            session.setTitle("Séance d'intégration - Groupe Pionniers");
            courseSessionRepository.save(session);
            CourseSession session02 = new CourseSession();
            session02.setSessionDate(java.time.LocalDate.now());
            session02.setTitle("Séance 02 - Groupe Pionniers");
            courseSessionRepository.save(session02);

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setCourseSession(session);
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendanceRepository.save(attendance);
        }

        
        System.out.println("SEEDING TERMINÉ : " + versesToSave.size() + " vers ont été générés.");
    }
    
}