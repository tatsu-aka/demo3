package com.example1.demo3.dto;

public class StockByMakerDto {
    private String productName;
    private String maker;
    private Long stock;

    public StockByMakerDto(String productName, String maker, Long stock) {
        this.productName = productName;
        this.maker = maker;
        this.stock = stock;
    }

    public String getProductName() {
        return productName;
    }

    public String getMaker() {
        return maker;
    }

    public Long getStock() {
        return stock;
    }

}
