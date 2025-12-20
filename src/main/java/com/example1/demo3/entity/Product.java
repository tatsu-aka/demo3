package com.example1.demo3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "cost_price", nullable = false)
    private Integer costPrice;

    @Column(name = "sale_price", nullable = false)
    private Integer salePrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---getter / setter ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {this.name = name; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getCostPrice() { return costPrice; }
    public void setCostPrice(Integer costPrice) { this.costPrice = costPrice; }

    public Integer getSalePrice() { return salePrice; }
    public void setSalePrice(Integer salePrice) { this.salePrice = salePrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() {return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
}
