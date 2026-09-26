package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.controller.api.StockDetailApiController;
import com.example1.demo3.dto.StockDetailByMakerDto;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.service.StockDetailService;

@ExtendWith(MockitoExtension.class)
class StockDetailApiControllerTest {

    @Mock
    private StockDetailService stockDetailService;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @InjectMocks
    private StockDetailApiController stockDetailApiController;

    @Test
    //正常系　在庫詳細削除
    void delete_shouldDelegateToService() {
        Integer stockDetailId = 1;

        stockDetailApiController.delete(stockDetailId);

        verify(stockDetailService).deleteAndUpdateProductStock(stockDetailId);
        verifyNoInteractions(stockDetailRepository);
    }

    @Test
    //例外系　在庫詳細削除　サービス例外の伝播
    void delete_shouldPropagateServiceException() {
        Integer stockDetailId = 1;
        RuntimeException expected = new RuntimeException("stock detail not found");
        doThrow(expected).when(stockDetailService).deleteAndUpdateProductStock(stockDetailId);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockDetailApiController.delete(stockDetailId));

        assertSame(expected, actual);
        verify(stockDetailService).deleteAndUpdateProductStock(stockDetailId);
    }

    @Test
    //正常系　在庫詳細取得
    void getDetail_shouldReturnDetailsForProduct() {
        Integer productId = 1;
        List<StockDetailByMakerDto> expected =
                List.of(new StockDetailByMakerDto("メーカーA", 5));
        when(stockDetailRepository.findDetailByProductId(productId)).thenReturn(expected);

        List<StockDetailByMakerDto> actual = stockDetailApiController.getDetail(productId);

        assertSame(expected, actual);
        verify(stockDetailRepository).findDetailByProductId(productId);
    }

    @Test
    //正常系　在庫詳細取得　在庫詳細が存在しない場合
    void getDetail_shouldReturnEmptyListWhenNoDetailsExist() {
        Integer productId = 1;
        List<StockDetailByMakerDto> expected = List.of();
        when(stockDetailRepository.findDetailByProductId(productId)).thenReturn(expected);

        List<StockDetailByMakerDto> actual = stockDetailApiController.getDetail(productId);

        assertSame(expected, actual);
        verify(stockDetailRepository).findDetailByProductId(productId);
    }

    @Test
    //例外系　在庫詳細取得　リポジトリ例外の伝播
    void getDetail_shouldPropagateRepositoryException() {
        Integer productId = 1;
        RuntimeException expected = new RuntimeException("stock detail lookup failed");
        when(stockDetailRepository.findDetailByProductId(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> stockDetailApiController.getDetail(productId));

        assertSame(expected, actual);
        verify(stockDetailRepository).findDetailByProductId(productId);
    }
}
