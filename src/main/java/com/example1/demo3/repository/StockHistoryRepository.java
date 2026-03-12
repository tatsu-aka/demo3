package com.example1.demo3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.dto.StockSummaryDto;
import com.example1.demo3.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {
        List<StockHistory> findByTypeOrderByDateTimeDesc(String type);

        List<StockHistory> findByProductIdAndTypeOrderByDateTimeAsc(Long productId, String type);

        @Modifying
        @Transactional
        @Query("DELETE FROM StockHistory h WHERE h.product.id = :productId")
        void deleteByProductId(@Param("productId") Integer productId);

        // 商品名で検索 出庫履歴
        @Query("SELECT h FROM StockHistory h " + "JOIN h.product p " + "WHERE h.type = 'OUT' "
                        + "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%')) "
                        + "ORDER BY h.dateTime DESC")
        List<StockHistory> searchOut(String keyword);

        // 商品名で検索 入庫履歴
        @Query("SELECT h FROM StockHistory h " + "JOIN h.product p " + "WHERE h.type = 'IN' "
                        + "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%')) "
                        + "ORDER BY h.dateTime DESC")
        List<StockHistory> searchIn(String keyword);

        // グラフ用：指定商品の履歴を昇順で取得
        List<StockHistory> findByProductIdOrderByDateTimeAsc(Integer productId);

        // 在庫一覧集計用
        @Query("""
                            SELECT new com.example1.demo3.dto.StockSummaryDto(
                                h.product.id,
                                h.product.name,
                                SUM(CASE WHEN h.type = 'IN' THEN h.quantity ELSE -h.quantity END)
                            )
                            FROM StockHistory h
                            GROUP BY h.product.id, h.product.name
                        """)
        List<StockSummaryDto> getStockSummary();

}
