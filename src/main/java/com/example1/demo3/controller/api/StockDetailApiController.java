package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.StockDetailByMakerDto;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.service.StockDetailService;

@RestController
@RequestMapping("/api/stock-detail")
public class StockDetailApiController {
    private final StockDetailService stockDetailService;
    private final StockDetailRepository stockDetailRepository;

    public StockDetailApiController(StockDetailService stockDetailService, StockDetailRepository stockDetailRepository) {
        this.stockDetailService = stockDetailService;
        this.stockDetailRepository = stockDetailRepository;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        stockDetailService.deleteAndUpdateProductStock(id);
    }

    @GetMapping("/{productId}")
    public List<StockDetailByMakerDto> getDetail(@PathVariable Integer productId) {
        return stockDetailRepository.findDetailByProductId(productId);
    }

}
