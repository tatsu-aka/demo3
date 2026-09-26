package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.controller.api.StockHistoryApiController;
import com.example1.demo3.dto.StockHistoryDto;
import com.example1.demo3.service.StockHistoryService;

@ExtendWith(MockitoExtension.class)
class StockHistoryApiControllerTest {

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockHistoryApiController stockHistoryApiController;

    @Test
    //正常系　在庫履歴取得
    void getStockHistory_shouldReturnStockHistoryForProduct() {
        Integer productId = 1;
        List<StockHistoryDto> expected = List.of(
                new StockHistoryDto(LocalDateTime.of(2026, 1, 1, 12, 0), 5));
        when(stockHistoryService.getStockHistoryForChart(productId)).thenReturn(expected);

        List<StockHistoryDto> actual = stockHistoryApiController.getStockHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getStockHistoryForChart(productId);
    }

    @Test
    //境界値　在庫履歴が存在しない場合
    void getStockHistory_shouldReturnEmptyListWhenNoHistoryExists() {
        Integer productId = 1;
        List<StockHistoryDto> expected = List.of();
        when(stockHistoryService.getStockHistoryForChart(productId)).thenReturn(expected);

        List<StockHistoryDto> actual = stockHistoryApiController.getStockHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getStockHistoryForChart(productId);
    }

    @Test
    //例外系　在庫履歴取得時のサービス例外
    void getStockHistory_shouldPropagateServiceException() {
        Integer productId = Integer.MIN_VALUE;
        RuntimeException expected = new RuntimeException("stock history lookup failed");
        when(stockHistoryService.getStockHistoryForChart(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockHistoryApiController.getStockHistory(productId));

        assertSame(expected, actual);
        verify(stockHistoryService).getStockHistoryForChart(productId);
    }
}
