package com.app.nidai.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.CourseSession;

public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
}