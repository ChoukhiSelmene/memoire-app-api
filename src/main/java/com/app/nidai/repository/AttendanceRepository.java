package com.app.nidai.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.Attendance;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Cette méthode nous sera très utile plus tard pour voir toutes les absences d'un élève
    List<Attendance> findByStudentShortId(String shortId);

    // Vérifie si un pointage existe déjà pour cet étudiant et cette séance
    boolean existsByStudentShortIdAndCourseSessionId(String shortId, Long courseSessionId);
}
