package com.example1.demo3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example1.demo3.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    
}
