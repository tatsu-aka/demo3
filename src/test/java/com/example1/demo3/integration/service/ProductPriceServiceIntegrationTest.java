package com.example1.demo3.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.service.ProductPriceService;

@SpringBootTest
@ActiveProfiles("test")
public class ProductPriceServiceIntegrationTest {
    
    @Autowired
    ProductPriceService priceService;

    @Autowired
    ProductPriceRepository priceRepository;

    @Autowired
    ProductRepository productRepository;

    @Test
    void testChangePrice() {

        //テストデータ
        Product product = new Product();
        product.setName("レタス");
        product.setUnit("個");
        product.setCategory("野菜");
        product.setStock(0);
        product = productRepository.save(product);

        Integer productId = product.getId();

        //過去の価格
        ProductPrice oldPrice = new ProductPrice();
        oldPrice.setProduct(product);
        oldPrice.setCostPrice(100);
        oldPrice.setStartDate(LocalDate.of(2023,1,1));
        oldPrice.setEndDate(null);
        priceRepository.save(oldPrice);

        //実行　changePrice
        LocalDate newStartDate = LocalDate.of(2024,1,1);
        int newCostPrice = 200;

        priceService.changePrice(productId, newCostPrice, newStartDate);

        //結果確認
        //現在の価格が新しい価格になっているか
        ProductPrice current = priceRepository.getCurrentPrice(productId);
        assertNotNull(current);
        assertEquals(200, current.getCostPrice());
        assertEquals(newStartDate, current.getStartDate());
        assertNull(current.getEndDate());

        //過去の価格が更新されているか
        ProductPrice updatedOld = priceRepository.findById(oldPrice.getId()).orElseThrow();
        assertEquals(LocalDate.of(2023,1,1), updatedOld.getStartDate());
        assertEquals(newStartDate.minusDays(1), updatedOld.getEndDate());

        //履歴が2件になっているか
        List<ProductPrice> history = priceRepository.getPriceHistory(productId);
        assertEquals(2, history.size());
    }
}
