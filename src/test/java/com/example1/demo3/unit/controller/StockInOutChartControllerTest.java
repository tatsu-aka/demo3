package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.StockInOutChartController;

class StockInOutChartControllerTest {

    private final StockInOutChartController stockInOutChartController =
            new StockInOutChartController();

    @Test
    // 正常値: 入出庫グラフ画面のビュー名を返す
    void showStockInOutpage_shouldReturnStockChartInOutView() {
        assertEquals("stock-chart-in-out", stockInOutChartController.showStockInOutpage());
    }

    @Test
    // 境界値: 状態を持たないため、繰り返し呼び出しても同じビュー名を返す
    void showStockInOutpage_shouldReturnSameViewWhenCalledRepeatedly() {
        assertEquals("stock-chart-in-out", stockInOutChartController.showStockInOutpage());
        assertEquals("stock-chart-in-out", stockInOutChartController.showStockInOutpage());
    }

    @Test
    // 異常値: 入力値がないため、例外を発生させずに処理できる
    void showStockInOutpage_shouldNotThrowException() {
        assertDoesNotThrow(() -> stockInOutChartController.showStockInOutpage());
    }
}