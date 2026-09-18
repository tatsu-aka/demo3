package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.ProductInOutController;

class ProductInOutControllerTest {

    private final ProductInOutController productInOutController = new ProductInOutController();

    @Test
    void showStockInPage_shouldReturnProductInView() {
        String viewName = productInOutController.showStockInPage();

        assertEquals("product-in", viewName);
    }

    @Test
    void showStockOutPage_shouldReturnProductOutView() {
        String viewName = productInOutController.showStockOutPage();

        assertEquals("product-out", viewName);
    }
}
