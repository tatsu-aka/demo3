package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.StockHistoryDto;
import com.example1.demo3.service.StockHistoryService;

@RestController
@RequestMapping("/api/stock-history")
public class StockHistoryApiController {

    private final StockHistoryService stockHistoryService;

    public StockHistoryApiController(StockHistoryService stockHistoryService) {
        this.stockHistoryService = stockHistoryService;
    }

    @GetMapping("/{productId}")
    public List<StockHistoryDto> getStockHistory(@PathVariable Integer productId) {
        return stockHistoryService.getStockHistoryForChart(productId);
    }
}
