package com.example1.demo3.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.dto.ProductMakerStockDto;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MakerRepository makerRepository;

    @Autowired
    private StockDetailRepository stockDetailRepository;

    @Test
    void findByNameContaining_returnsProductsWithMatchingName() {
        Product apple = createProduct("青森りんご", "果物");
        Product grape = createProduct("青森ぶどう", "果物");
        createProduct("みかん", "果物");

        List<Product> result = productRepository.findByNameContaining("青森");

        assertEquals(Set.of(apple.getId(), grape.getId()),
                result.stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet()));
        assertEquals(List.of(), productRepository.findByNameContaining("該当なし"));
    }

    @Test
    void findByCategory_returnsProductsWithExactCategory() {
        Product apple = createProduct("りんご", "果物");
        Product grape = createProduct("ぶどう", "果物");
        createProduct("にんじん", "野菜");

        List<Product> result = productRepository.findByCategory("果物");

        assertEquals(Set.of(apple.getId(), grape.getId()),
                result.stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet()));
        assertEquals(List.of(), productRepository.findByCategory("該当なし"));
    }

    @Test
    void findProductMakerStockList_returnsJoinedResultsInOrder() {
        Product firstProduct = createProduct("商品A", "食品");
        Product secondProduct = createProduct("商品B", "食品");
        createProduct("在庫明細なし", "食品");

        Maker makerC = createMaker("C社");
        Maker makerA = createMaker("A社");
        Maker makerB = createMaker("B社");

        saveStockDetail(firstProduct, makerC, 10);
        saveStockDetail(firstProduct, makerA, 20);
        saveStockDetail(secondProduct, makerB, 30);

        List<ProductMakerStockDto> result = productRepository.findProductMakerStockList();

        assertEquals(List.of("商品A", "商品A", "商品B"),
                result.stream().map(ProductMakerStockDto::productName).toList());
        assertEquals(List.of("A社", "C社", "B社"),
                result.stream().map(ProductMakerStockDto::makerName).toList());
        assertEquals(List.of(20, 10, 30),
                result.stream().map(ProductMakerStockDto::quantity).toList());
    }

    @Test
    void findProductMakerStockList_returnsEmptyListWhenNoStockDetailsExist() {
        createProduct("商品A", "食品");

        assertEquals(List.of(), productRepository.findProductMakerStockList());
    }

    private Product createProduct(String name, String category) {
        Product product = new Product();
        product.setName(name);
        product.setUnit("個");
        product.setCategory(category);
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