package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example1.demo3.controller.PriceChangeController;

class PriceChangeControllerTest {

    private final PriceChangeController priceChangeController = new PriceChangeController();

    @Test
    // 正常値: 価格変更画面のビュー名を返す
    void priceChange_shouldReturnPriceChangeView() {
        assertEquals("price-change", priceChangeController.priceChange());
    }

    @Test
    // 正常値: 価格履歴画面のビュー名を返す
    void priceHistory_shouldReturnPriceHistoryView() {
        assertEquals("price-history", priceChangeController.priceHistory());
    }

    @Test
    // 境界値: 状態を持たないため、繰り返し呼び出しても同じビュー名を返す
    void priceChange_shouldReturnSameViewWhenCalledRepeatedly() {
        assertEquals("price-change", priceChangeController.priceChange());
        assertEquals("price-change", priceChangeController.priceChange());
    }

    @Test
    // 異常値: 入力値がないため、例外を発生させずに処理できる
    void priceHistory_shouldNotThrowException() {
        assertDoesNotThrow(() -> priceChangeController.priceHistory());
    }
}