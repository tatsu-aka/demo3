package com.example1.demo3.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockHistoryRepository;

import jakarta.transaction.Transactional;

@Service
public class StockService {
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public StockService(ProductRepository productRepository,
                        StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }
    // 商品取得
    private Product findProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));
    }

    // 入庫処理
    @Transactional
    public void inStock(Integer id, Integer quantity, String maker, String unit) {
        Product product = findProduct(id);

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        saveHistory(product, quantity, "IN", maker, unit);
    }
    // 出庫処理
    @Transactional
    public void outStock(Integer id, Integer quantity) {
        Product product = findProduct(id);

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("在庫が足りません");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        saveHistory(product, quantity, "OUT", product.getMaker().getName(), product.getUnit());
    }
    // 履歴保存（共通）
    private void saveHistory(Product product, Integer quantity, String type, String maker, String unit) {
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setType(type);
        history.setMaker(maker);
        history.setUnit(unit);
        history.setDateTime(LocalDateTime.now());

        stockHistoryRepository.save(history);
    }
}
