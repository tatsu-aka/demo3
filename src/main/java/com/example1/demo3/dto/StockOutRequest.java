package com.example1.demo3.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockOutRequest {
    @NotNull(message = "商品IDは必須です")
    private Integer productId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は１以上で入力してください")
    private Integer quantity;

    @NotBlank(message = "単位は必須です")
    private String unit;

    @NotBlank(message = "カテゴリは必須です")
    private String category;

    public Integer getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getCategory() { return category; }
}
