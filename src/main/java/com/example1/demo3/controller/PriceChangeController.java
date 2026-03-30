package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PriceChangeController {
    
    @GetMapping("/price-change")
    public String priceChange() {
        return "price-change";
    }
}
