package com.app.nidai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_session_id"})
})
@Getter @Setter @NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_session_id", nullable = false)
    private CourseSession courseSession;

    // L'annotation @Enumerated(EnumType.STRING) est cruciale : 
    // elle force Hibernate à écrire "PRESENT" ou "ABSENT" en toutes lettres dans la base, 
    // plutôt que 0 ou 1, ce qui rend la base de données lisible pour un humain.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}