package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example1.demo3.service.ProductService;

@Controller
@RequestMapping("/products")
public class StockChartController {
    
    private final ProductService productService;

    public StockChartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/stock-chart")
    public String stockChartPage(Model model) {
        model.addAttribute("products", productService.findAll());
        return "stock-chart";
    }
}
