package com.example1.demo3.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.example1.demo3.dto.ProductMakerStockDto;
import com.example1.demo3.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(Integer id);

    List<Product> findByNameContaining(String keyword);

    List<Product> findByCategory(String category);

    @Query("""
                SELECT new com.example1.demo3.dto.ProductMakerStockDto(
                    sd.id,
                    p.id,
                    p.name,
                    m.name,
                    sd.quantity,
                    p.unit,
                    p.category
                )
                FROM Product p
                JOIN StockDetail sd ON sd.product.id = p.id
                JOIN Maker m ON m.id = sd.maker.id
                ORDER BY p.id, m.name
            """)
    List<ProductMakerStockDto> findProductMakerStockList();

}
