package com.example1.demo3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example1.demo3.dto.StockByMakerDto;
import com.example1.demo3.dto.StockSummaryDto;
import com.example1.demo3.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer> {
    List<StockHistory> findByTypeOrderByDateTimeDesc(String type);

    List<StockHistory> findByProductIdAndTypeOrderByDateTimeAsc(Integer productId, String type);

    // 商品名で検索 出庫履歴
        @Query("SELECT h FROM StockHistory h " + "LEFT JOIN h.product p " + "WHERE h.type = 'OUT' "
            + "AND (:keyword IS NULL OR :keyword = '' OR COALESCE(h.productName, p.name) LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY h.dateTime DESC")
    List<StockHistory> searchOut(String keyword);

    // 商品名で検索 入庫履歴
        @Query("SELECT h FROM StockHistory h " + "LEFT JOIN h.product p " + "WHERE h.type = 'IN' "
            + "AND (:keyword IS NULL OR :keyword = '' OR COALESCE(h.productName, p.name) LIKE CONCAT('%', :keyword, '%')) "
            + "ORDER BY h.dateTime DESC")
    List<StockHistory> searchIn(String keyword);

    // グラフ用：指定商品の履歴を昇順で取得
    List<StockHistory> findByProductIdOrderByDateTimeAsc(Integer productId);

    @Modifying
    @Query("UPDATE StockHistory h SET h.productName = h.product.name WHERE h.product.id = :id AND h.productName IS NULL")
    void snapshotProductName(Integer id);

    // 在庫一覧集計用（商品合計）
    @Query("""
                SELECT new com.example1.demo3.dto.StockSummaryDto(
                    COALESCE(h.productName, h.product.name),
                    SUM(CASE WHEN h.type = 'IN' THEN h.quantity ELSE -h.quantity END)
                )
                FROM StockHistory h
                GROUP BY COALESCE(h.productName, h.product.name)
            """)
    List<StockSummaryDto> getStockSummary();

    // 在庫一覧集計用（取引先別）
    @Query("""
                SELECT new com.example1.demo3.dto.StockByMakerDto(
                    COALESCE(h.productName, h.product.name),
                    h.maker.name,
                    SUM(CASE WHEN h.type = 'IN' THEN h.quantity ELSE -h.quantity END)
                )
                FROM StockHistory h
                WHERE COALESCE(h.productName, h.product.name) = :productName
                GROUP BY COALESCE(h.productName, h.product.name), h.maker.name
            """)
    List<StockByMakerDto> getStockByMaker(String productName);

    @Modifying
    @Query("UPDATE StockHistory sh SET sh.product = NULL WHERE sh.product.id = :id")
    void clearProductId(Integer id);

}
