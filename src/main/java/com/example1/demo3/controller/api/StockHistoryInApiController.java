package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.StockHistoryInDto;
import com.example1.demo3.service.StockHistoryService;

@RestController
@RequestMapping("/api/stock-history/in")
public class StockHistoryInApiController {

    private final StockHistoryService service;

    public StockHistoryInApiController(StockHistoryService service) {
        this.service = service;
    }

    @GetMapping("/{productId}")
    public List<StockHistoryInDto> getInHistory(@PathVariable Long productId) {
        return service.getInHistory(productId);
    }
}
