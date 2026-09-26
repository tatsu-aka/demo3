package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example1.demo3.controller.api.StockApiController;
import com.example1.demo3.dto.StockByMakerDto;
import com.example1.demo3.dto.StockInRequest;
import com.example1.demo3.dto.StockOutRequest;
import com.example1.demo3.dto.StockSummaryDto;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockInService;
import com.example1.demo3.service.StockOutService;

@ExtendWith(MockitoExtension.class)
class StockApiControllerTest {

    @Mock
    private StockInService stockInService;

    @Mock
    private StockOutService stockOutService;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @InjectMocks
    private StockApiController stockApiController;

    @Mock
    private BindingResult bindingResult;

    @Test
    //正常系　入庫処理
    void stockIn_shouldDelegateRequestAndReturnOk() {
        StockInRequest request = org.mockito.Mockito.mock(StockInRequest.class);
        when(request.getProductId()).thenReturn(1);
        when(request.getQuantity()).thenReturn(1);
        when(request.getMakerId()).thenReturn(10);
        when(request.getUnit()).thenReturn("個");
        when(request.getCategory()).thenReturn("野菜");

        ResponseEntity<?> actual = stockApiController.stockIn(request, bindingResult);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals("ok", actual.getBody());
        verify(stockInService).inStock(1, 1, 10, "個", "野菜");
    }

    @Test
    //異常系　入庫処理　バリデーションエラー
    void stockIn_shouldReturnBadRequestWhenValidationFails() {
        ObjectError error = new ObjectError("stockInRequest", "数量は１以上で入力してください");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<?> actual = stockApiController.stockIn(
                org.mockito.Mockito.mock(StockInRequest.class), bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals(List.of("数量は１以上で入力してください"), actual.getBody());
        verifyNoInteractions(stockInService);
    }

    @Test
    //例外系　入庫処理　サービス例外の伝播
    void stockIn_shouldPropagateServiceException() {
        StockInRequest request = org.mockito.Mockito.mock(StockInRequest.class);
        when(request.getProductId()).thenReturn(1);
        when(request.getQuantity()).thenReturn(1);
        when(request.getMakerId()).thenReturn(10);
        when(request.getUnit()).thenReturn("個");
        when(request.getCategory()).thenReturn("野菜");
        RuntimeException expected = new RuntimeException("stock in failed");
        org.mockito.Mockito.doThrow(expected).when(stockInService)
                .inStock(1, 1, 10, "個", "野菜");

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockApiController.stockIn(request, bindingResult));

        assertSame(expected, actual);
    }

    @Test
    //正常系　出庫処理
    void stockOut_shouldDelegateRequestAndReturnOk() {
        StockOutRequest request = org.mockito.Mockito.mock(StockOutRequest.class);
        when(request.getProductId()).thenReturn(1);
        when(request.getQuantity()).thenReturn(1);
        when(request.getUnit()).thenReturn("個");
        when(request.getCategory()).thenReturn("野菜");
        when(request.getMakerId()).thenReturn(10);

        ResponseEntity<?> actual = stockApiController.stockOut(request, bindingResult);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals("ok", actual.getBody());
        verify(stockOutService).outStock(1, 1, "個", "野菜", 10);
    }

    @Test
    //異常系　出庫処理　バリデーションエラー
    void stockOut_shouldReturnBadRequestWhenValidationFails() {
        ObjectError error = new ObjectError("stockOutRequest", "商品IDは必須です");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<?> actual = stockApiController.stockOut(
                org.mockito.Mockito.mock(StockOutRequest.class), bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals(List.of("商品IDは必須です"), actual.getBody());
        verifyNoInteractions(stockOutService);
    }

    @Test
    //例外系　出庫処理　サービス例外の伝播
    void stockOut_shouldPropagateServiceException() {
        StockOutRequest request = org.mockito.Mockito.mock(StockOutRequest.class);
        when(request.getProductId()).thenReturn(1);
        when(request.getQuantity()).thenReturn(1);
        when(request.getUnit()).thenReturn("個");
        when(request.getCategory()).thenReturn("野菜");
        when(request.getMakerId()).thenReturn(10);
        RuntimeException expected = new RuntimeException("stock out failed");
        org.mockito.Mockito.doThrow(expected).when(stockOutService)
                .outStock(1, 1, "個", "野菜", 10);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockApiController.stockOut(request, bindingResult));

        assertSame(expected, actual);
    }

    @Test
    //正常系　在庫集計取得
    void summary_shouldReturnStockSummaryList() {
        List<StockSummaryDto> expected = List.of(new StockSummaryDto("レタス", 5L));
        when(stockHistoryRepository.getStockSummary()).thenReturn(expected);

        List<StockSummaryDto> actual = stockApiController.summary();

        assertSame(expected, actual);
        verify(stockHistoryRepository).getStockSummary();
    }

    @Test
    //異常系　在庫が存在しない場合
    void summary_shouldReturnEmptyListWhenNoStockExists() {
        List<StockSummaryDto> expected = List.of();
        when(stockHistoryRepository.getStockSummary()).thenReturn(expected);

        List<StockSummaryDto> actual = stockApiController.summary();

        assertSame(expected, actual);
        assertEquals(0, actual.size());
    }

    @Test
    //例外系　在庫集計取得　リポジトリ例外の伝播
    void summary_shouldPropagateRepositoryException() {
        RuntimeException expected = new RuntimeException("stock summary failed");
        when(stockHistoryRepository.getStockSummary()).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockApiController.summary());

        assertSame(expected, actual);
    }

    @Test
    //正常系　メーカー別在庫取得
    void getStockByMaker_shouldReturnStockBreakdown() {
        String productName = "レタス";
        List<StockByMakerDto> expected = List.of(new StockByMakerDto(productName, "メーカーA", 5L));
        when(stockHistoryRepository.getStockByMaker(productName)).thenReturn(expected);

        List<StockByMakerDto> actual = stockApiController.getStockByMaker(productName);

        assertSame(expected, actual);
        verify(stockHistoryRepository).getStockByMaker(productName);
    }

    @Test
    //異常系　メーカー別在庫取得　在庫が存在しない場合
    void getStockByMaker_shouldReturnEmptyListWhenNoStockExists() {
        String productName = "レタス";
        List<StockByMakerDto> expected = List.of();
        when(stockHistoryRepository.getStockByMaker(productName)).thenReturn(expected);

        List<StockByMakerDto> actual = stockApiController.getStockByMaker(productName);

        assertSame(expected, actual);
        assertEquals(0, actual.size());
    }

    @Test
    //例外系　メーカー別在庫取得　リポジトリ例外の伝播
    void getStockByMaker_shouldPropagateRepositoryException() {
        String productName = "レタス";
        RuntimeException expected = new RuntimeException("maker stock lookup failed");
        when(stockHistoryRepository.getStockByMaker(productName)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockApiController.getStockByMaker(productName));

        assertSame(expected, actual);
    }
}
