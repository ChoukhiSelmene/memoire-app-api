package com.app.nidai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.nidai.entity.Poem;

public interface PoemRepository extends JpaRepository<Poem, Long> {
    
}
