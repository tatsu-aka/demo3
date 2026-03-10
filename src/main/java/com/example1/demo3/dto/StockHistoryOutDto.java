package com.example1.demo3.dto;

import java.time.LocalDateTime;

public class StockHistoryOutDto {
    private LocalDateTime dateTime;
    private Integer quantity;

    public StockHistoryOutDto(LocalDateTime dateTime, Integer quantity) {
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
