package com.example1.demo3.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.service.ProductPriceService;

@RestController
@RequestMapping("/api/products")
public class ProductPriceApiController {
    
    private final ProductPriceService priceService;
    private final ProductPriceRepository priceRepository;

    public ProductPriceApiController(ProductPriceService priceService, ProductPriceRepository priceRepository) {
        this.priceService = priceService;
        this.priceRepository = priceRepository;
    }

    //価格変更　API
    @PostMapping("/{id}/price")
    public void changePrice(@PathVariable("id") Integer productId, @RequestBody ChangePriceRequest request) {
        priceService.changePrice(productId, request.newCostPrice(), request.startDate());
    }

    //履歴取得　API
    @GetMapping("/{id}/price-history")
    public List<ProductPrice> getPriceHistory(@PathVariable("id") Integer productId) {
        return priceRepository.getPriceHistory(productId);
    }

    // DTOリクエスト用
    public static record ChangePriceRequest(Integer newCostPrice, LocalDate startDate) {}

}
