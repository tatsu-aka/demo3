package com.example1.demo3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@Service
public class StockOutService {
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public StockOutService(ProductRepository productRepository, StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    @Transactional
    public void outStock(Integer productId, Integer quantity, String unit, String category) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));
        
        //在庫不足チェック
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("在庫が不足しています");
        }

        //在庫更新
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        //履歴保存
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setType("OUT");
        history.setUnit(unit);
        history.setCategory(category);

        stockHistoryRepository.save(history);
    }
}
