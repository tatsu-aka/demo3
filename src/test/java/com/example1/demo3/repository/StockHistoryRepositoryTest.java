package com.example1.demo3.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.dto.StockByMakerDto;
import com.example1.demo3.dto.StockSummaryDto;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockHistory;

@DataJpaTest
@ActiveProfiles("test")
class StockHistoryRepositoryTest {

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MakerRepository makerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    //正常系　商品IDとタイプで検索し、日付順に返す
    void derivedQueries_filterByTypeAndProductAndReturnDateOrder() {
        Product product = createProduct("商品A");
        Product otherProduct = createProduct("商品B");
        StockHistory earlierIn = saveHistory(product, "IN", 10, "商品A", null);
        StockHistory laterIn = saveHistory(product, "IN", 20, "商品A", null);
        StockHistory out = saveHistory(product, "OUT", 3, "商品A", null);
        StockHistory otherProductIn = saveHistory(otherProduct, "IN", 5, "商品B", null);

        setDateTime(earlierIn, LocalDateTime.of(2026, 1, 1, 9, 0));
        setDateTime(laterIn, LocalDateTime.of(2026, 1, 2, 9, 0));
        setDateTime(out, LocalDateTime.of(2026, 1, 3, 9, 0));
        setDateTime(otherProductIn, LocalDateTime.of(2026, 1, 4, 9, 0));

        assertEquals(List.of(otherProductIn.getId(), laterIn.getId(), earlierIn.getId()),
                stockHistoryRepository.findByTypeOrderByDateTimeDesc("IN").stream()
                        .map(StockHistory::getId).toList());
        assertEquals(List.of(earlierIn.getId(), laterIn.getId()),
                stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(product.getId(), "IN").stream()
                        .map(StockHistory::getId).toList());
        assertEquals(List.of(earlierIn.getId(), laterIn.getId(), out.getId()),
                stockHistoryRepository.findByProductIdOrderByDateTimeAsc(product.getId()).stream()
                        .map(StockHistory::getId).toList());
        assertEquals(List.of(), stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(-1, "IN"));
    }

    @Test
    //正常系　商品名で検索し、日付順に返す。部分一致、null、空文字もサポート
    void searchInAndOut_supportPartialNameNullAndEmptyKeywords() {
        Product product = createProduct("りんご");
        StockHistory inByProductName = saveHistory(product, "IN", 10, null, null);
        StockHistory inBySnapshot = saveHistory(null, "IN", 4, "青森りんご", null);
        StockHistory out = saveHistory(product, "OUT", 2, "商品名スナップショット", null);
        saveHistory(product, "OUT", 1, "別商品", null);

        setDateTime(inByProductName, LocalDateTime.of(2026, 2, 1, 9, 0));
        setDateTime(inBySnapshot, LocalDateTime.of(2026, 2, 2, 9, 0));
        setDateTime(out, LocalDateTime.of(2026, 2, 3, 9, 0));

        assertEquals(List.of(inBySnapshot.getId(), inByProductName.getId()),
                stockHistoryRepository.searchIn("りんご").stream().map(StockHistory::getId).toList());
        assertEquals(List.of(inBySnapshot.getId(), inByProductName.getId()),
                stockHistoryRepository.searchIn(null).stream().map(StockHistory::getId).toList());
        assertEquals(List.of(inBySnapshot.getId(), inByProductName.getId()),
                stockHistoryRepository.searchIn("").stream().map(StockHistory::getId).toList());
        assertEquals(List.of(), stockHistoryRepository.searchOut("該当なし"));
        assertEquals(List.of(out.getId()),
                stockHistoryRepository.searchOut("商品名スナップショット").stream().map(StockHistory::getId).toList());
    }

    @Test
    //履歴の入出庫数を集計し、商品別・取引先別に正しい数量を返すことを確認
    void stockSummaryAndMakerSummary_calculateNetQuantities() {
        Product product = createProduct("商品A");
        Maker makerA = createMaker("メーカーA");
        Maker makerB = createMaker("メーカーB");
        saveHistory(product, "IN", 10, "商品A", makerA);
        saveHistory(product, "OUT", 3, "商品A", makerA);
        saveHistory(product, "IN", 5, "商品A", makerB);
        saveHistory(null, "IN", 7, "旧商品名", makerA);

        Map<String, Long> stockByProduct = stockHistoryRepository.getStockSummary().stream()
                .collect(Collectors.toMap(StockSummaryDto::getProductName, StockSummaryDto::getStock));
        assertEquals(Map.of("商品A", 12L, "旧商品名", 7L), stockByProduct);

        Map<String, Long> stockByMaker = stockHistoryRepository.getStockByMaker("商品A").stream()
                .collect(Collectors.toMap(StockByMakerDto::getMaker, StockByMakerDto::getStock));
        assertEquals(Map.of("メーカーA", 7L, "メーカーB", 5L), stockByMaker);
        assertEquals(List.of(), stockHistoryRepository.getStockByMaker("存在しない商品"));
    }

    @Test
    //正常系　商品名スナップショットを作成し、商品IDをクリアしてもスナップショットが保持されることを確認
    void snapshotProductNameAndClearProductId_preserveHistoricalName() {
        Product product = createProduct("商品A");
        StockHistory history = saveHistory(product, "IN", 5, null, null);

        stockHistoryRepository.snapshotProductName(product.getId());
        entityManager.flush();
        entityManager.clear();

        StockHistory snapshotted = stockHistoryRepository.findById(history.getId()).orElseThrow();
        assertEquals("商品A", snapshotted.getProductName());

        stockHistoryRepository.clearProductId(product.getId());
        entityManager.flush();
        entityManager.clear();

        StockHistory detachedFromProduct = stockHistoryRepository.findById(history.getId()).orElseThrow();
        assertNull(detachedFromProduct.getProduct());
        assertEquals("商品A", detachedFromProduct.getProductName());
    }

    private Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setUnit("個");
        product.setCategory("食品");
        product.setStock(0);
        return productRepository.saveAndFlush(product);
    }

    private Maker createMaker(String name) {
        Maker maker = new Maker();
        maker.setName(name);
        return makerRepository.saveAndFlush(maker);
    }

    private StockHistory saveHistory(Product product, String type, int quantity, String productName, Maker maker) {
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setType(type);
        history.setQuantity(quantity);
        history.setProductName(productName);
        history.setMaker(maker);
        return stockHistoryRepository.saveAndFlush(history);
    }

    private void setDateTime(StockHistory history, LocalDateTime dateTime) {
        entityManager.createNativeQuery("UPDATE stock_history SET date_time = :dateTime WHERE id = :id")
                .setParameter("dateTime", dateTime)
                .setParameter("id", history.getId())
                .executeUpdate();
        entityManager.clear();
    }
}
