package com.app.nidai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByShortId(String shortId);

}
