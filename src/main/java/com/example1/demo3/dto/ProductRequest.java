package com.example1.demo3.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {
    
    @NotBlank(message = "商品名は必須です")
    private String name;

    @NotBlank(message = "カテゴリは必須です")
    private String category;

    @NotBlank(message = "単位は必須です")
    private String unit;

    @NotNull(message = "原価は必須です")
    @Min(value = 0, message = "原価は０以上で入力してください")
    private Integer costPrice;

    @NotNull(message = "取引先は必須です")
    private Integer makerId;

    //getter
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public Integer getCostPrice() { return costPrice; }
    public Integer getMakerId() { return makerId; }

    //setter
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCostPrice(Integer costPrice) { this.costPrice = costPrice; }
    public void setMakerId(Integer makerId) { this.makerId = makerId; }
}
