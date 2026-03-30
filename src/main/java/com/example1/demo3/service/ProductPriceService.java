package com.example1.demo3.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductPriceService {
    
    private final ProductPriceRepository priceRepository;
    private final ProductRepository productRepository;

    public ProductPriceService(ProductPriceRepository priceRepository, ProductRepository productRepository) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
    }
    //価格変更処理
    @Transactional
    public void changePrice(Integer productId, Integer newCostPrice, LocalDate starDate) {

        //現在価格の取得
        ProductPrice current = priceRepository.getCurrentPrice(productId);

        //現在の価格が存在する場合　end_dateを閉じる
        if (current != null) {
            current.setEndDate(starDate.minusDays(1));
            priceRepository.save(current);
        }

        //新しい価格追加
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("商品がみつかりません"));

        ProductPrice newPrice = new ProductPrice();
        newPrice.setProduct(product);
        newPrice.setCostPrice(newCostPrice);
        newPrice.setStartDate(starDate);
        
        priceRepository.save(newPrice);
    }
}
