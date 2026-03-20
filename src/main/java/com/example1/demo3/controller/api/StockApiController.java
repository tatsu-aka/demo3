package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.StockInRequest;
import com.example1.demo3.dto.StockOutRequest;
import com.example1.demo3.dto.StockSummaryDto;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockInService;
import com.example1.demo3.service.StockOutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stock")
public class StockApiController {
    private final StockInService stockInService;
    private final StockOutService stockOutService;
    private final StockHistoryRepository stockHistoryRepository;

    public StockApiController(StockInService stockInService,
        StockOutService stockOutService, StockHistoryRepository stockHistoryRepository) {
        this.stockInService = stockInService;
        this.stockOutService = stockOutService;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // 入庫API
    @PostMapping("/in")
    public ResponseEntity<?> stockIn(@Valid @RequestBody StockInRequest req, BindingResult result) {
        if (result.hasErrors()) {
            List<String> messages = result.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(messages);
        }

        stockInService.inStock(req.getProductId(), req.getQuantity(), req.getMakerId(), req.getUnit(),
                req.getCategory());
        return ResponseEntity.ok("ok");
    }

    // 出庫API
    @PostMapping("/out")
    public ResponseEntity<?> stockOut(@Valid @RequestBody StockOutRequest req, BindingResult result) {
        if (result.hasErrors()) {
            List<String> messages = result.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(messages);
        }

        stockOutService.outStock(req.getProductId(), req.getQuantity(), req.getUnit(), req.getCategory());
        return ResponseEntity.ok("ok");
    }

    // 在庫一覧用
    @GetMapping("/summary")
    public List<StockSummaryDto> summary() {
        return stockHistoryRepository.getStockSummary();
    }
}
