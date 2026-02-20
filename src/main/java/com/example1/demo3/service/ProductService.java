package com.example1.demo3.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public ProductService(ProductRepository productRepository,
                          StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // 商品一覧
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // 商品保存（新規・更新どちらも対応）
    public void save(Product product) {
        productRepository.save(product);
    }

    // 商品削除
    @Transactional
    public void deleteById(Integer id) {
        stockHistoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    // 商品取得
    public Product findById(Integer id) {
        return productRepository.findById(id).orElseThrow();
    }

    // 入庫処理
    @Transactional
    public void inStock(Integer id, Integer quantity, String maker, String unit) {
        Product product = findById(id);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        //入庫履歴に保存
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setMaker(maker);
        history.setUnit(unit);

        stockHistoryRepository.save(history);
    }

    // 出庫処理
    @Transactional
    public void outStock(Integer id, Integer quantity) {
        Product product = findById(id);

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("在庫が足りません");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        saveHistory(product, quantity, "OUT");
    }

    // 履歴保存（共通化）
    private void saveHistory(Product product, Integer quantity, String type) {
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setType(type);
        history.setDateTime(LocalDateTime.now());
        stockHistoryRepository.save(history);
    }

    // キーワード検索
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContaining(keyword);
    }
}