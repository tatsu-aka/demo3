package com.example1.demo3.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.entity.Maker;

@DataJpaTest
@ActiveProfiles("test")
class ProductPriceRepositoryTest {

    @Autowired
    private ProductPriceRepository priceRepository;

    @Autowired
    private ProductRepository productRepository;

        @Autowired
        private MakerRepository makerRepository;

    @Test
    // 現在の価格が取得できることを確認する
    void getCurrentPrice_returnsOpenEndedPriceForSpecifiedProduct() {
        Product product = createProduct("商品A");
        Product anotherProduct = createProduct("商品B");

        savePrice(product, 100, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        ProductPrice current = savePrice(
                product, 200, LocalDate.of(2024, 2, 1), null);
        savePrice(anotherProduct, 300, LocalDate.of(2024, 2, 1), null);

        assertEquals(current.getId(),
                priceRepository.getCurrentPrice(product.getId()).getId());
        assertNull(priceRepository.getCurrentPrice(999999));
    }

    @Test
    // 価格履歴が開始日付の降順で取得できることを確認する
    void getPriceHistory_returnsPricesInDescendingStartDateOrder() {
        Product product = createProduct("商品A");
        Product anotherProduct = createProduct("商品B");

        savePrice(product, 100, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        savePrice(product, 200, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));
        savePrice(product, 300, LocalDate.of(2024, 3, 1), null);
        savePrice(anotherProduct, 400, LocalDate.of(2024, 4, 1), null);

        List<LocalDate> startDates = priceRepository.getPriceHistory(product.getId())
                .stream()
                .map(ProductPrice::getStartDate)
                .toList();

        assertEquals(List.of(
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 1, 1)), startDates);
        assertEquals(List.of(), priceRepository.getPriceHistory(999999));
    }

    @Test
    // 指定日付の価格が開始日付と終了日付を含み、無期限価格をサポートすることを確認する
    void getPriceAt_includesStartAndEndDatesAndSupportsOpenEndedPrice() {
        Product product = createProduct("商品A");
        ProductPrice first = savePrice(
                product, 100, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        ProductPrice current = savePrice(
                product, 200, LocalDate.of(2024, 2, 1), null);

        assertEquals(first.getId(), priceRepository.getPriceAt(
                product.getId(), LocalDate.of(2024, 1, 1)).getId());
        assertEquals(first.getId(), priceRepository.getPriceAt(
                product.getId(), LocalDate.of(2024, 1, 31)).getId());
        assertEquals(current.getId(), priceRepository.getPriceAt(
                product.getId(), LocalDate.of(2024, 2, 1)).getId());
        assertEquals(current.getId(), priceRepository.getPriceAt(
                product.getId(), LocalDate.of(2030, 1, 1)).getId());

        assertNull(priceRepository.getPriceAt(
                product.getId(), LocalDate.of(2023, 12, 31)));
        assertNull(priceRepository.getPriceAt(
                999999, LocalDate.of(2024, 1, 1)));
    }

    @Test
    // 価格期間が重複している場合に例外がスローされることを確認する
    void getPriceAt_throwsWhenPricePeriodsOverlap() {
        Product product = createProduct("商品A");
        savePrice(product, 100, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        savePrice(product, 200, LocalDate.of(2024, 2, 1), null);

        assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> priceRepository.getPriceAt(
                        product.getId(), LocalDate.of(2024, 2, 1)));
    }

    private Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setUnit("個");
        product.setCategory("食品");
        product.setStock(0);
        Maker maker = new Maker();
        maker.setName(name + "メーカー");
        product.setMaker(makerRepository.save(maker));
        return productRepository.save(product);
    }

    private ProductPrice savePrice(
            Product product, int costPrice, LocalDate startDate, LocalDate endDate) {
        ProductPrice price = new ProductPrice();
        price.setProduct(product);
        price.setCostPrice(costPrice);
        price.setStartDate(startDate);
        price.setEndDate(endDate);
        return priceRepository.save(price);
    }
}