package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.StockListController;

class StockListControllerTest {

    private final StockListController stockListController = new StockListController();

    @Test
    void stockList_shouldReturnStockListView() {
        String viewName = stockListController.stockList();

        assertEquals("stock-list", viewName);
    }

    @Test
    void stockList_shouldNotThrowException() {
        assertDoesNotThrow(() -> stockListController.stockList());
    }
}
