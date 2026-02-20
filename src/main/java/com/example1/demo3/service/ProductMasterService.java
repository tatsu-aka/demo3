package com.example1.demo3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;

@Service
public class ProductMasterService {
    private final ProductRepository productRepository;
    private final MakerRepository makerRepository;

    public ProductMasterService(ProductRepository productRepository, MakerRepository makerRepository) {
        this.productRepository = productRepository;
        this.makerRepository = makerRepository;
    }

    // 商品マスタ一覧
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // 商品取得（編集用）
    public Product findById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));
    }
    // 商品保存（新規・更新）
    public void save(Product product) {

        // Maker（取引先）を ID から取得してセット
        if (product.getMaker() != null && product.getMaker().getId() != null) {
            Maker maker = makerRepository.findById(product.getMaker().getId()).orElseThrow(() -> new IllegalArgumentException("取引先が見つかりません"));
            product.setMaker(maker);
        }

        // Unit は文字列なのでそのまま
        productRepository.save(product);
    }

    // 商品削除
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }


}
