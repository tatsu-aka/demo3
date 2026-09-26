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

import com.example1.demo3.controller.api.StockHistoryInApiController;
import com.example1.demo3.dto.StockHistoryInDto;
import com.example1.demo3.service.StockHistoryService;

@ExtendWith(MockitoExtension.class)
class StockHistoryInApiControllerTest {

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockHistoryInApiController stockHistoryInApiController;

    @Test
    //正常系　在庫入庫履歴取得
    void getInHistory_shouldReturnInHistoryForProduct() {
        Integer productId = 1;
        List<StockHistoryInDto> expected = List.of(
                new StockHistoryInDto(LocalDateTime.of(2026, 1, 1, 12, 0), 5));
        when(stockHistoryService.getInHistory(productId)).thenReturn(expected);

        List<StockHistoryInDto> actual = stockHistoryInApiController.getInHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getInHistory(productId);
    }

    @Test
    //境界値　在庫入庫履歴が存在しない場合
    void getInHistory_shouldReturnEmptyListWhenNoHistoryExistsForBoundaryProductId() {
        Integer productId = 0;
        List<StockHistoryInDto> expected = List.of();
        when(stockHistoryService.getInHistory(productId)).thenReturn(expected);

        List<StockHistoryInDto> actual = stockHistoryInApiController.getInHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getInHistory(productId);
    }

    @Test
    //例外系　在庫入庫履歴取得時にサービス層で例外が発生した場合
    void getInHistory_shouldPropagateServiceException() {
        Integer productId = 1;
        RuntimeException expected = new RuntimeException("in history lookup failed");
        when(stockHistoryService.getInHistory(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockHistoryInApiController.getInHistory(productId));

        assertSame(expected, actual);
        verify(stockHistoryService).getInHistory(productId);
    }
}