package com.example1.demo3.dto;

public class StockSummaryDto {
    private String productName;
    private Long stock;

    public StockSummaryDto(String productName, Long stock) {
        this.productName = productName;
        this.stock = stock;
    }

    public String getProductName() { return productName; }
    public Long getStock() { return stock; }
}
