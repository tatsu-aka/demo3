package com.example1.demo3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example1.demo3.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {
    List<StockHistory> findByTypeOrderByDateTimeDesc(String type);
    
}
