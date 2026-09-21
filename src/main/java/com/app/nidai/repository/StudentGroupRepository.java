package com.app.nidai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.StudentGroup;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    
}
