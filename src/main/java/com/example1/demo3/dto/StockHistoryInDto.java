package com.example1.demo3.dto;

import java.time.LocalDateTime;

public class StockHistoryInDto {
    private LocalDateTime dateTime;
    private Integer quantuty;

    public StockHistoryInDto(LocalDateTime dateTime, Integer quantity) {
        this.dateTime = dateTime;
        this.quantuty = quantity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Integer getQuantity() {
        return quantuty;
    }
}
