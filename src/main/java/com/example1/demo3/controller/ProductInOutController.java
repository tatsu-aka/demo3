package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductInOutController {
    
    @GetMapping("/in")
    public String showStockInPage() {
        return "product-in";
    }

    @GetMapping("/out")
    public String showStockOutPage() {
        return "product-out";
    }
}
