package com.example1.demo3.dto;

public class ProductDto {
    private Integer id;
    private String name;
    private String category;
    private String unit;
    private Integer stock;

    public ProductDto(Integer id, String name, String category, String unit, Integer stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.stock = stock;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public Integer getStock() { return stock; }

}
