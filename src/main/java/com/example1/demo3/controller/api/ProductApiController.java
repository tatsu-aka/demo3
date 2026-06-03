package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.ProductMakerStockDto;

import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;
    private final StockDetailRepository stockDetailRepository;

    public ProductApiController(ProductService productService, StockDetailRepository stockDetailRepository) {
        this.productService = productService;
        this.stockDetailRepository = stockDetailRepository;
    }

    @GetMapping("/list-by-maker")
    public List<ProductMakerStockDto> listByMaker() {
        return productService.findProductMakerStockList();
    }
}
