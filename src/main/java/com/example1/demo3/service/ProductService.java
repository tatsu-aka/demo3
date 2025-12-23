package com.example1.demo3.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.ProductRepository;


@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;

    }

    // 商品を保存するメゾット
    public void saveProduct(String name, int stock, int costprice, int saleprice) {

        Product p = new Product();
        p.setName(name);
        p.setStock(stock);
        p.setCostPrice(costprice);
        p.setSalePrice(saleprice);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        productRepository.save(p);

    }

    public List<Product> findAllProducts() {
       
        return productRepository.findAll();
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    public void outStock(Integer id, Integer quantity) {
        Product product = productRepository.findById(id).orElseThrow();

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("在庫が足りません");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    public Product findById(Integer id) {
        return productRepository.findById(id).orElseThrow();
    }

    
}
