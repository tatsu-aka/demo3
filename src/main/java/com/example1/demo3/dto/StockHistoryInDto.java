package com.example1.demo3.dto;

import java.time.LocalDateTime;

public class StockHistoryInDto {
    private LocalDateTime dateTime;
    private Integer quantity;

    public StockHistoryInDto(LocalDateTime dateTime, Integer quantity) {
        this.dateTime = dateTime;
        this.quantity = quantity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
