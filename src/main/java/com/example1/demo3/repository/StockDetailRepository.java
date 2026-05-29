package com.example1.demo3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.entity.StockDetail;

public interface StockDetailRepository extends JpaRepository<StockDetail, Integer> {

    Optional<StockDetail> findByProductIdAndMakerId(Integer productId, Integer makerId);

    List<StockDetail> findByProductId(Integer productId);

    void deleteByProductId(Integer productId);

    @Modifying
    @Query("UPDATE StockDetail sd SET sd.product = NULL WHERE sd.product.id = :id")
    void clearProductId(Integer id);

    @Query("""
                SELECT new com.example1.demo3.dto.ProductDto(
                    p.id,
                    p.name,
                    p.category,
                    p.unit,
                    sd.quantity,
                    m.name
                )
                FROM StockDetail sd
                JOIN sd.product p
                JOIN sd.maker m
                ORDER BY p.id, m.id
            """)
    List<ProductDto> findAllForList();

}