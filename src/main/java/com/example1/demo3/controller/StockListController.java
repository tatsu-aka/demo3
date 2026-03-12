package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stock")
public class StockListController {

    @GetMapping("/list")
    public String stockList() {
        return "stock-list"; 
    }

}
