package com.example1.demo3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.dto.ProductRequest;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final MakerRepository makerRepository;

    public ProductService(ProductRepository productRepository, StockHistoryRepository stockHistoryRepository,
            MakerRepository makerRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.makerRepository = makerRepository;
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

    //新規登録（API）
    public void create(ProductRequest req) {
        Product p = new Product();
        applyRequestToEntity(req, p);
        p.setStock(0);
        productRepository.save(p);
    }

    //更新（API)
    public void update(Integer id, ProductRequest req) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        applyRequestToEntity(req, p);
        productRepository.save(p);
    }

    //Entityへ変換
    private void applyRequestToEntity(ProductRequest req, Product p) {
        p.setName(req.getName());
        p.setCategory(req.getCategory());
        p.setUnit(req.getUnit());
        p.setCostPrice(req.getCostPrice());

        Maker maker = makerRepository.findById(req.getMakerId()).orElseThrow(() -> new RuntimeException("Maker not found"));
        p.setMaker(maker);
    }
    
    //出力用DTO
    public List<ProductDto> findAllDto() {
        return productRepository.findAll().stream().map(p -> new ProductDto(p.getId(), p.getName(), p.getCategory(),
                p.getUnit(), p.getStock(), p.getMaker() != null ? p.getMaker().getName() : null)).toList();
    }
}