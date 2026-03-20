package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.StockHistoryOutDto;
import com.example1.demo3.service.StockHistoryService;

@RestController
@RequestMapping("/api/stock-history/out")
public class StockHistoryOutApiController {
    
    private final StockHistoryService service;

    public StockHistoryOutApiController(StockHistoryService service) {
        this.service = service;
    }
    @GetMapping("/{productId}")
    public List<StockHistoryOutDto> getOutHistory(@PathVariable Long productId) {
        return service.getOutHistory(productId);
    }
}
