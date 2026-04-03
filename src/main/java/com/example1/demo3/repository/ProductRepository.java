package com.example1.demo3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByNameContaining(String keyword);

    List<Product> findByCategory(String category);

    @Query("""
                SELECT new com.example1.demo3.dto.ProductDto(
                    p.id,
                    p.name,
                    p.category,
                    p.unit,
                    p.stock,
                    m.name
                )
                FROM Product p
                LEFT JOIN p.maker m
                ORDER BY p.id
            """)
    List<ProductDto> findAllDto();

}
