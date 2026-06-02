package com.example1.demo3.dto;

public record ProductMakerStockDto(
    Integer stockDetailId,
    Integer productId,
    String productName,
    String makerName,
    Integer quantity,
    String unit,
    String category
) {}
