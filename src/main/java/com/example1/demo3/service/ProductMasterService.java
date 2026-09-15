package com.example1.demo3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.exception.ResourceNotFoundException;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@Service
public class ProductMasterService {
    private final ProductRepository productRepository;
    private final MakerRepository makerRepository;
    private final StockDetailRepository stockDetailRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public ProductMasterService(ProductRepository productRepository, MakerRepository makerRepository,
            StockDetailRepository stockDetailRepository, StockHistoryRepository stockHistoryRepository) {
        this.productRepository = productRepository;
        this.makerRepository = makerRepository;
        this.stockDetailRepository = stockDetailRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // 商品マスタ一覧
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // 商品取得（編集用）
    public Product findById(Integer id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません"));
    }

    // 商品保存（新規・更新）
    public Product save(Product product) {

        if (product.getId() != null) {
            Product existing = productRepository.findById(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません"));
            product.setCreatedAt(existing.getCreatedAt());
        }

        // Maker（取引先）を ID から取得してセット
        if (product.getMaker() != null && product.getMaker().getId() != null) {
            Maker maker = makerRepository.findById(product.getMaker().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("取引先が見つかりません"));
            product.setMaker(maker);
        }

        // Unit は文字列なのでそのまま
        return productRepository.save(product);
    }

    // 商品削除
    @Transactional
    public void delete(Integer id) {
        productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品が見つかりません"));
        stockDetailRepository.deleteByProductId(id);
        stockHistoryRepository.snapshotProductName(id);
        stockHistoryRepository.clearProductId(id);
        productRepository.deleteById(id);
    }

}
