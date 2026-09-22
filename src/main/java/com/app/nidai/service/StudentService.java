package com.app.nidai.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.nidai.dto.AttendanceDto;
import com.app.nidai.dto.AttendanceRequest;
import com.app.nidai.dto.VerseDto;
import com.app.nidai.entity.Attendance;
import com.app.nidai.entity.CourseSession;
import com.app.nidai.entity.Student;
import com.app.nidai.entity.Verse;
import com.app.nidai.repository.AttendanceRepository;
import com.app.nidai.repository.CourseSessionRepository;
import com.app.nidai.repository.StudentRepository;
import com.app.nidai.repository.VerseRepository;

@Service 
public class StudentService {

    private final StudentRepository studentRepository;
    private final VerseRepository verseRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseSessionRepository courseSessionRepository;

    public StudentService(StudentRepository studentRepository, VerseRepository verseRepository, AttendanceRepository attendanceRepository, CourseSessionRepository courseSessionRepository) {
        this.studentRepository = studentRepository;
        this.verseRepository = verseRepository;
        this.attendanceRepository = attendanceRepository;
        this.courseSessionRepository = courseSessionRepository;
    }

    public List<VerseDto> getValidatedVerses(String shortId) {
        
        // 1. Utilisation de l'Optional : Si l'étudiant n'existe pas, on lève une exception proprement.
        Student student = studentRepository.findByShortId(shortId)
            .orElseThrow(() -> new IllegalArgumentException("Etudiant introuvable : " + shortId));
        
        // 2. Gestion du cas où l'étudiant vient de commencer et n'a pas encore de curseur
        if (student.getLastValidatedVerse() == null) {
            return Collections.emptyList();
        }

        // 3. Récupération de l'index magique
        Integer targetIndex = student.getLastValidatedVerse().getLearningIndex();

        // 4. Utilisation du Stream pour mapper les entités vers nos DTO immuables
        return verseRepository.findByLearningIndexLessThanEqualOrderByLearningIndexAsc(targetIndex)
                .stream()
                .map(verse -> new VerseDto(
                        verse.getChapter().getNumber(),
                        verse.getVerseNumber(),
                        verse.getIsEndOfChapter()
                ))
                .toList();
    
    }

    public List<AttendanceDto> getStudentAttendances(String shortId) {
        // 1. On vérifie d'abord que l'étudiant existe, sinon on lève une exception (comme pour les acquis)
        studentRepository.findByShortId(shortId)
                .orElseThrow(() -> new IllegalArgumentException("Etudiant introuvable : " + shortId));  
        // 2. On récupère la liste des entités (pointages brut)
        // 3. On utilise l'API Stream pour les transformer en objets immuables et légers (DTO)
        return attendanceRepository.findByStudentShortId(shortId)
                .stream()
                .map(attendance -> new AttendanceDto(
                        attendance.getCourseSession().getSessionDate(),
                        attendance.getCourseSession().getTitle(),
                        attendance.getStatus()
                ))
                .toList();
    }

    @Transactional
    public VerseDto validateNextVerse(String shortId) {
        // 1. Récupération de l'étudiant
        Student student = studentRepository.findByShortId(shortId)
            .orElseThrow(() -> new IllegalArgumentException("Etudiant introuvable : " + shortId));

        // 2. Calcul du prochain index
        int nextIndex = 1; // Par défaut, s'il n'a rien validé, il commence au vers 1
        if(student.getLastValidatedVerse() != null) {
            nextIndex = student.getLastValidatedVerse().getLearningIndex() + 1;
        }

        // 3. Récupération du prochain vers
        Verse nextVerse = verseRepository.findByLearningIndex(nextIndex)
            .orElseThrow(() -> new IllegalStateException("Le livre est terminé ou le vers n'existe pas."));

        // 4. Mise à jour du curseur
        student.setLastValidatedVerse(nextVerse);

        // 5. Logique Métier : S'il s'agit du dernier vers du chapitre, on incrémente son palier
        if(nextVerse.getIsEndOfChapter()) {
            student.setCurrentPalier(student.getCurrentPalier() + 1);
        }

        // 6. Sauvegarde en base de données
        studentRepository.save(student);

        // 7. On retourne le DTO du vers nouvellement validé
        return new VerseDto(
            nextVerse.getChapter().getNumber(),
            nextVerse.getVerseNumber(),
            nextVerse.getIsEndOfChapter()
        );
    }

    @Transactional
    public AttendanceDto addAttendance(String shortId, AttendanceRequest request) {
        // 1. Récupération de l'étudiant
        Student student = studentRepository.findByShortId(shortId)
            .orElseThrow(() -> new IllegalArgumentException("Etudiant introuvable : " + shortId));

        // 2. Récupération de la séance grâce à l'ID reçu dans le JSON
        CourseSession session = courseSessionRepository.findById(request.courseSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Seance introuvable : " + shortId));
        
        // 2.5 Contrôle métier : Vérifier que le pointage n'existe pas déjà
        if(attendanceRepository.existsByStudentShortIdAndCourseSessionId(shortId, request.courseSessionId())) {
            throw new IllegalStateException("Un pointage existe deja pour cet étudiant a cette seance.");
        }

        // 3. Création et hydratation de la nouvelle entité Attendance
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourseSession(session);
        attendance.setStatus(request.status());

        // 4. Sauvegarde en base de données
        attendanceRepository.save(attendance);

        // 5. On retourne un DTO pour confirmer au client que l'enregistrement a réussi
        return new AttendanceDto(
            session.getSessionDate(),
            session.getTitle(),
            attendance.getStatus());
    }
}
