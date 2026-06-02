package com.example1.demo3.service;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;

import jakarta.transaction.Transactional;

@Service
public class StockDetailService {

    private final StockDetailRepository stockDetailRepository;
    private final ProductRepository productRepository;

    public StockDetailService(StockDetailRepository stockDetailRepository, ProductRepository productRepository) {
        this.stockDetailRepository = stockDetailRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void deleteAndUpdateProductStock(Integer stockDetailId) {

        //削除対象の StockDetail を取得
        StockDetail sd = stockDetailRepository.findById(stockDetailId)
                .orElseThrow(() -> new RuntimeException("在庫が見つかりません"));

        Product product = sd.getProduct();

        //StockDetail を削除
        stockDetailRepository.delete(sd);

        //残っている StockDetail の数量を合計して Product.stock を更新
        Integer newTotal = stockDetailRepository.sumQuantityByProductId(product.getId());
        if (newTotal == null)
            newTotal = 0;

        product.setStock(newTotal);
        productRepository.save(product);
    }
}
