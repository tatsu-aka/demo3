package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockInOutChartController {
    @GetMapping("/stock-in-out")
    public String showStockInOutpage() {
        return "stock-chart-in-out";
    }
}
