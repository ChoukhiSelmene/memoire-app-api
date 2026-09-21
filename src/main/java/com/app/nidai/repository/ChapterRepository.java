package com.app.nidai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
}
