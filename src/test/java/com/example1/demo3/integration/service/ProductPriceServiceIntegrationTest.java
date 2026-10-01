package com.example1.demo3.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.dto.ProductPriceDto;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.exception.ResourceNotFoundException;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.service.ProductPriceService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductPriceServiceIntegrationTest {

    @Autowired
    private ProductPriceService priceService;

    @Autowired
    private ProductPriceRepository priceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MakerRepository makerRepository;

    @Test
    // 価格変更処理が正しく行われることを確認する
    void changePrice_closesCurrentPriceAndAddsNewPrice() {
        Product product = createProduct("レタス", null);
        ProductPrice oldPrice = savePrice(product, 100,
                LocalDate.of(2023, 1, 1), null);
        LocalDate newStartDate = LocalDate.of(2024, 1, 1);

        priceService.changePrice(product.getId(), 200, newStartDate);

        ProductPrice current = priceRepository.getCurrentPrice(product.getId());
        assertNotNull(current);
        assertEquals(200, current.getCostPrice());
        assertEquals(newStartDate, current.getStartDate());
        assertNull(current.getEndDate());

        ProductPrice updatedOld = priceRepository.findById(oldPrice.getId()).orElseThrow();
        assertEquals(LocalDate.of(2023, 1, 1), updatedOld.getStartDate());
        assertEquals(newStartDate.minusDays(1), updatedOld.getEndDate());

        List<ProductPrice> history = priceRepository.getPriceHistory(product.getId());
        assertEquals(2, history.size());
        assertEquals(List.of(newStartDate, LocalDate.of(2023, 1, 1)),
                history.stream().map(ProductPrice::getStartDate).toList());
    }

    @Test
    // 現在の価格がない場合に新しい価格を追加することを確認する
    void changePrice_addsPriceWhenNoCurrentPriceExists() {
        Product product = createProduct("トマト", null);
        LocalDate startDate = LocalDate.of(2025, 4, 1);

        priceService.changePrice(product.getId(), 300, startDate);

        ProductPrice current = priceRepository.getCurrentPrice(product.getId());
        assertNotNull(current);
        assertEquals(300, current.getCostPrice());
        assertEquals(startDate, current.getStartDate());
        assertNull(current.getEndDate());
        assertEquals(1, priceRepository.getPriceHistory(product.getId()).size());
    }

    @Test
    // 商品が存在しない場合に例外がスローされることを確認する
    void changePrice_throwsWhenProductDoesNotExist() {
        Integer unknownProductId = 999999;

        assertThrows(ResourceNotFoundException.class,
                () -> priceService.changePrice(
                        unknownProductId, 200, LocalDate.of(2025, 1, 1)));

        assertNull(priceRepository.getCurrentPrice(unknownProductId));
        assertEquals(0, priceRepository.getPriceHistory(unknownProductId).size());
    }

        @Test
        // 現在価格の開始日以前の日付で価格変更できないことを確認する
        void changePrice_throwsWhenStartDateIsNotAfterCurrentPriceStartDate() {
        Product product = createProduct("キャベツ", null);
        LocalDate currentStartDate = LocalDate.of(2024, 2, 1);
        ProductPrice currentPrice = savePrice(product, 100, currentStartDate, null);

        assertThrows(IllegalArgumentException.class,
            () -> priceService.changePrice(product.getId(), 200, currentStartDate));
        assertThrows(IllegalArgumentException.class,
            () -> priceService.changePrice(
                product.getId(), 200, currentStartDate.minusDays(1)));

        ProductPrice unchangedPrice = priceRepository.findById(currentPrice.getId()).orElseThrow();
        assertNull(unchangedPrice.getEndDate());
        assertEquals(1, priceRepository.getPriceHistory(product.getId()).size());
        }

        @Test
        // 価格切り替え日の前日までは旧価格、当日から新価格になることを確認する
        void changePrice_switchesPriceAtStartDateBoundary() {
        Product product = createProduct("白菜", null);
        LocalDate newStartDate = LocalDate.of(2024, 3, 1);
        savePrice(product, 100, newStartDate.minusMonths(1), null);

        priceService.changePrice(product.getId(), 200, newStartDate);

        assertEquals(100, priceRepository.getPriceAt(
            product.getId(), newStartDate.minusDays(1)).getCostPrice());
        assertEquals(200, priceRepository.getPriceAt(
            product.getId(), newStartDate).getCostPrice());
        }

    @Test
    // 価格履歴が商品名とメーカー名を含むことを確認する
    void getHistory_returnsPriceHistoryWithProductAndMakerNames() {
        Maker maker = new Maker();
        maker.setName("テストメーカー");
        maker = makerRepository.save(maker);

        Product product = createProduct("きゅうり", maker);
        savePrice(product, 150, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        savePrice(product, 180, LocalDate.of(2024, 2, 1), null);

        List<ProductPriceDto> history = priceService.getHistory(product.getId());

        assertEquals(2, history.size());
        assertEquals(180, history.get(0).costPrice());
        assertEquals(LocalDate.of(2024, 2, 1), history.get(0).startDate());
        assertNull(history.get(0).endDate());
        assertEquals("きゅうり", history.get(0).productName());
        assertEquals("テストメーカー", history.get(0).makerName());

        assertEquals(150, history.get(1).costPrice());
        assertEquals(LocalDate.of(2024, 1, 1), history.get(1).startDate());
        assertEquals(LocalDate.of(2024, 1, 31), history.get(1).endDate());
    }

    @Test
    // 価格履歴がない商品の履歴取得で空リストを返すことを確認する
    void getHistory_returnsEmptyListWhenProductHasNoPriceHistory() {
        Product product = createProduct("未登録商品", null);

        List<ProductPriceDto> history = priceService.getHistory(product.getId());

        assertNotNull(history);
        assertEquals(List.of(), history);
    }

    private Product createProduct(String name, Maker maker) {
        if (maker == null) {
            maker = new Maker();
            maker.setName(name + "メーカー");
            maker = makerRepository.save(maker);
        }
        Product product = new Product();
        product.setName(name);
        product.setUnit("個");
        product.setCategory("野菜");
        product.setStock(0);
        product.setMaker(maker);
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
