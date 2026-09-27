package com.example1.demo3.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.dto.StockDetailByMakerDto;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;

@DataJpaTest
@ActiveProfiles("test")
class StockDetailRepositoryTest {

    @Autowired
    private StockDetailRepository stockDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MakerRepository makerRepository;

    @Test
    // 指定された商品IDとメーカーIDに一致する在庫明細を返すことを確認する
    void findByProductIdAndMakerId_returnsMatchingDetail() {
        Product product = createProduct("商品A");
        Maker makerA = createMaker("A社");
        Maker makerB = createMaker("B社");

        StockDetail expected = saveStockDetail(product, makerA, 10);
        saveStockDetail(product, makerB, 20);

        assertEquals(expected.getId(),
                stockDetailRepository.findByProductIdAndMakerId(
                        product.getId(), makerA.getId()).orElseThrow().getId());
        assertTrue(stockDetailRepository.findByProductIdAndMakerId(
                product.getId(), 999999).isEmpty());
    }

    @Test
    // 指定された商品IDに一致する在庫明細のみを返すことを確認する
    void findByProductId_returnsOnlyDetailsForSpecifiedProduct() {
        Product product = createProduct("商品A");
        Product anotherProduct = createProduct("商品B");
        Maker maker = createMaker("A社");

        StockDetail first = saveStockDetail(product, maker, 10);
        StockDetail second = saveStockDetail(product, maker, 20);
        saveStockDetail(anotherProduct, maker, 30);

        List<StockDetail> result = stockDetailRepository.findByProductId(product.getId());

        assertEquals(Set.of(first.getId(), second.getId()),
                result.stream()
                        .map(StockDetail::getId)
                        .collect(Collectors.toSet()));
        assertEquals(List.of(), stockDetailRepository.findByProductId(999999));
    }

    @Test
    // 指定された商品IDに一致する在庫明細のみを削除することを確認する
    void deleteByProductId_deletesOnlyDetailsForSpecifiedProduct() {
        Product product = createProduct("商品A");
        Product anotherProduct = createProduct("商品B");
        Maker maker = createMaker("A社");

        saveStockDetail(product, maker, 10);
        StockDetail remaining = saveStockDetail(anotherProduct, maker, 20);

        stockDetailRepository.deleteByProductId(product.getId());

        assertEquals(List.of(), stockDetailRepository.findByProductId(product.getId()));
        assertEquals(remaining.getId(),
                stockDetailRepository.findByProductId(anotherProduct.getId())
                        .get(0).getId());
    }

    @Test
    // 指定された商品IDに一致する在庫明細のメーカー名と数量を、メーカー名の昇順で返すことを確認する
    void findDetailByProductId_returnsMakerDetailsInNameOrder() {
        Product product = createProduct("商品A");
        Product anotherProduct = createProduct("商品B");
        Maker makerC = createMaker("C社");
        Maker makerA = createMaker("A社");
        Maker makerB = createMaker("B社");

        saveStockDetail(product, makerC, 30);
        saveStockDetail(product, makerA, 10);
        saveStockDetail(anotherProduct, makerB, 20);

        List<StockDetailByMakerDto> result =
                stockDetailRepository.findDetailByProductId(product.getId());

        assertEquals(List.of("A社", "C社"),
                result.stream().map(StockDetailByMakerDto::makerName).toList());
        assertEquals(List.of(10, 30),
                result.stream().map(StockDetailByMakerDto::quantity).toList());
        assertEquals(List.of(), stockDetailRepository.findDetailByProductId(999999));
    }

    @Test
    // 指定された商品IDに一致する在庫明細の数量の合計を返すことを確認する
    void sumQuantityByProductId_returnsSumZeroOrNullAsAppropriate() {
        Product product = createProduct("商品A");
        Product productWithZeroQuantity = createProduct("商品B");
        Maker makerA = createMaker("A社");
        Maker makerB = createMaker("B社");

        saveStockDetail(product, makerA, 10);
        saveStockDetail(product, makerB, 20);
        saveStockDetail(productWithZeroQuantity, makerA, 0);

        assertEquals(30, stockDetailRepository.sumQuantityByProductId(product.getId()));
        assertEquals(0,
                stockDetailRepository.sumQuantityByProductId(productWithZeroQuantity.getId()));
        assertNull(stockDetailRepository.sumQuantityByProductId(999999));
    }

    private Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setUnit("個");
        product.setCategory("食品");
        product.setStock(0);
        return productRepository.save(product);
    }

    private Maker createMaker(String name) {
        Maker maker = new Maker();
        maker.setName(name);
        return makerRepository.save(maker);
    }

    private StockDetail saveStockDetail(Product product, Maker maker, int quantity) {
        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(quantity);
        return stockDetailRepository.save(detail);
    }
}