package com.example1.demo3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example1.demo3.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {
    List<StockHistory> findByTypeOrderByDateTimeDesc(String type);

    //商品名で検索 出庫履歴
    @Query("SELECT h FROM StockHistory h " + "JOIN h.product p " + 
            "WHERE h.type = 'OUT' " + "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY h.dateTime DESC")
    List<StockHistory> searchOut(String keyword);

    //商品名で検索 入庫履歴
    @Query("SELECT h FROM StockHistory h " + "JOIN h.product p " + 
            "WHERE h.type = 'IN' " + "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY h.dateTime DESC")
    List<StockHistory> searchIn(String keyword);
    
}
