package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDto> getProducts() {
        return productService.findAllDto();
    }
}
