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

import com.example1.demo3.controller.api.StockHistoryOutApiController;
import com.example1.demo3.dto.StockHistoryOutDto;
import com.example1.demo3.service.StockHistoryService;

@ExtendWith(MockitoExtension.class)
class StockHistoryOutApiControllerTest {

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockHistoryOutApiController stockHistoryOutApiController;

    @Test
    //正常系　在庫出庫履歴取得
    void getOutHistory_shouldReturnOutHistoryForProduct() {
        Integer productId = 1;
        List<StockHistoryOutDto> expected = List.of(
                new StockHistoryOutDto(LocalDateTime.of(2026, 1, 1, 12, 0), 5));
        when(stockHistoryService.getOutHistory(productId)).thenReturn(expected);

        List<StockHistoryOutDto> actual = stockHistoryOutApiController.getOutHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getOutHistory(productId);
    }

    @Test
    //境界値　在庫出庫履歴が存在しない場合
    void getOutHistory_shouldReturnEmptyListWhenNoHistoryExistsForBoundaryProductId() {
        Integer productId = 0;
        List<StockHistoryOutDto> expected = List.of();
        when(stockHistoryService.getOutHistory(productId)).thenReturn(expected);

        List<StockHistoryOutDto> actual = stockHistoryOutApiController.getOutHistory(productId);

        assertSame(expected, actual);
        verify(stockHistoryService).getOutHistory(productId);
    }

    @Test
    //例外系　在庫出庫履歴取得時にサービス層で例外が発生した場合
    void getOutHistory_shouldPropagateServiceException() {
        Integer productId = 1;
        RuntimeException expected = new RuntimeException("out history lookup failed");
        when(stockHistoryService.getOutHistory(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockHistoryOutApiController.getOutHistory(productId));

        assertSame(expected, actual);
        verify(stockHistoryService).getOutHistory(productId);
    }
}