package com.example1.demo3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example1.demo3.entity.Maker;

public interface MakerRepository extends JpaRepository<Maker, Integer> {
    
}

