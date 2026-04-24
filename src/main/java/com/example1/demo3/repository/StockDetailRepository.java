package com.example1.demo3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example1.demo3.entity.StockDetail;

public interface StockDetailRepository extends JpaRepository<StockDetail, Integer> {

    Optional<StockDetail> findByProductIdAndMakerId(Integer productId, Integer makerId);

    List<StockDetail> findByProductId(Integer productId);
}