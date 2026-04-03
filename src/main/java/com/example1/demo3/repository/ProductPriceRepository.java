package com.example1.demo3.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example1.demo3.entity.ProductPrice;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Integer> {
    // 現在の価格
    @Query("""
            SELECT p
            FROM ProductPrice p
            WHERE p.product.id = :productId
            AND p.endDate IS NULL
            """)
    ProductPrice getCurrentPrice(Integer productId);

    //価格履歴一覧
    @Query("""
            SELECT p
            FROM ProductPrice p
            WHERE p.product.id = :productId
            ORDER BY p.startDate DESC
            """)
    List<ProductPrice> getPriceHistory(Integer productId);

    //指定日の価格
    @Query("""
            SELECT p
            FROM ProductPrice p
            WHERE p.product.id = :productId
            AND p.startDate <= :targetDate
            AND (p.endDate IS NULL OR p.endDate >= :targetDate)
            ORDER BY p.startDate DESC
            """)
    ProductPrice getPriceAt(Integer productId, LocalDate targetDate);
}
