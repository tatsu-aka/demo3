package com.example1.demo3.dto;

import java.time.LocalDate;

public record ProductPriceDto(
    Integer costPrice,
    LocalDate startDate,
    LocalDate endDate,
    String productName,
    String makerName
) {}
