package com.example1.demo3.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String listProduct(Model model) {
        var products = productService.findAllProducts();
        model.addAttribute("products", products);
        return "product-list";
    }
    
    @GetMapping("/test-save")
    public String testSave() {
        Product p = new Product();
        p.setName("リンゴ");
        p.setStock(10);
        p.setCostPrice(500);
        p.setSalePrice(800);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        productService.save(p);

        return "redirect:/products";
        
    }
    
}
