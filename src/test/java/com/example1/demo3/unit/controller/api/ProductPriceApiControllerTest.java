package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.controller.api.ProductPriceApiController;
import com.example1.demo3.controller.api.ProductPriceApiController.ChangePriceRequest;
import com.example1.demo3.dto.ProductPriceDto;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.service.ProductPriceService;

@ExtendWith(MockitoExtension.class)
class ProductPriceApiControllerTest {

    @Mock
    private ProductPriceService priceService;

    @Mock
    private ProductPriceRepository priceRepository;

    @InjectMocks
    private ProductPriceApiController productPriceApiController;

    @Test
    //境界値　価格１の境界値
    void changePrice_shouldDelegateRequestValuesToService() {
        Integer productId = 1;
        Integer newCostPrice = 1;
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        ChangePriceRequest request = new ChangePriceRequest(newCostPrice, startDate);

        productPriceApiController.changePrice(productId, request);

        verify(priceService).changePrice(productId, newCostPrice, startDate);
    }

    @Test
    //例外系　価格変更
    void changePrice_shouldPropagateServiceException() {
        Integer productId = Integer.MIN_VALUE;
        ChangePriceRequest request = new ChangePriceRequest(100, LocalDate.of(2026, 1, 1));
        RuntimeException expected = new RuntimeException("price change failed");
        doThrow(expected).when(priceService)
                .changePrice(productId, request.newCostPrice(), request.startDate());

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> productPriceApiController.changePrice(productId, request));

        assertSame(expected, actual);
        verify(priceService).changePrice(productId, request.newCostPrice(), request.startDate());
    }

    @Test
    //正常系　価格履歴取得
    void getPriceHistory_shouldReturnPriceHistory() {
        Integer productId = 1;
        ProductPriceDto historyEntry = new ProductPriceDto(
                200, LocalDate.of(2026, 1, 1), null, "レタス", "メーカーA");
        List<ProductPriceDto> expected = List.of(historyEntry);
        when(priceService.getHistory(productId)).thenReturn(expected);

        List<ProductPriceDto> actual = productPriceApiController.getPriceHistory(productId);

        assertSame(expected, actual);
        verify(priceService).getHistory(productId);
    }

    @Test
    //境界値　価格履歴が存在しない場合
    void getPriceHistory_shouldReturnEmptyListWhenNoHistoryExists() {
        Integer productId = 1;
        List<ProductPriceDto> expected = List.of();
        when(priceService.getHistory(productId)).thenReturn(expected);

        List<ProductPriceDto> actual = productPriceApiController.getPriceHistory(productId);

        assertSame(expected, actual);
        verify(priceService).getHistory(productId);
    }

    @Test
    //例外系　価格履歴取得
    void getPriceHistory_shouldPropagateServiceException() {
        Integer productId = Integer.MIN_VALUE;
        RuntimeException expected = new RuntimeException("price history lookup failed");
        when(priceService.getHistory(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> productPriceApiController.getPriceHistory(productId));

        assertSame(expected, actual);
        verify(priceService).getHistory(productId);
    }
}