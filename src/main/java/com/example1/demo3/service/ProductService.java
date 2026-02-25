package com.example1.demo3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Product;
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

    // キーワード検索
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContaining(keyword);
    }
}