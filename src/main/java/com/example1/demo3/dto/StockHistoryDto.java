package com.example1.demo3.dto;

import java.time.LocalDateTime;

public class StockHistoryDto {
    private LocalDateTime dateTime;
    private Integer stock;

    public StockHistoryDto(LocalDateTime dateTime, Integer stock) {
        this.dateTime = dateTime;
        this.stock = stock;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Integer getStock() {
        return stock;
    }

}
