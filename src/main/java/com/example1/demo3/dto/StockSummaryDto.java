package com.example1.demo3.dto;

public class StockSummaryDto {
    private Integer productId;
    private String productName;
    private Long stock;

    public StockSummaryDto(Integer productId, String productName, Long stock) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getStock() { return stock; }
}
