package com.example1.demo3.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example1.demo3.dto.ProductPriceDto;
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
    public void changePrice(Integer productId, Integer newCostPrice, LocalDate startDate) {

        //現在価格の取得
        ProductPrice current = priceRepository.getCurrentPrice(productId);

        //現在の価格が存在する場合　end_dateを閉じる
        if (current != null) {
            current.setEndDate(startDate.minusDays(1));
            priceRepository.save(current);
        }

        //新しい価格追加
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("商品がみつかりません"));

        ProductPrice newPrice = new ProductPrice();
        newPrice.setProduct(product);
        newPrice.setCostPrice(newCostPrice);
        newPrice.setStartDate(startDate);
        
        priceRepository.save(newPrice);
    }

    //価格変更履歴取得
    public List<ProductPriceDto> getHistory(Integer productId) {
        return priceRepository.getPriceHistory(productId).stream()
        .map(p -> new ProductPriceDto(
            p.getCostPrice(), p.getStartDate(), p.getEndDate(), p.getProduct().getName(), p.getProduct().getMaker().getName()
        )).toList();
    }
}
