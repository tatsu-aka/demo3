package com.example1.demo3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;

@ExtendWith(MockitoExtension.class)
public class StockDetailServiceTest {
    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockDetailService stockDetailService;

    // 正常系
    @Test
    void deleteAndUpdateProductStock_shouldUpdateProductStock() {

        // 準備
        Product product = new Product();
        product.setId(1);
        product.setStock(20); // 初期値（後で上書きされる）

        StockDetail sd = new StockDetail();
        sd.setId(10);
        sd.setProduct(product);

        when(stockDetailRepository.findById(10)).thenReturn(Optional.of(sd));

        // StockDetail の合計数量
        when(stockDetailRepository.sumQuantityByProductId(1)).thenReturn(15);

        // 実行
        stockDetailService.deleteAndUpdateProductStock(10);

        // 検証
        // 更新
        assertEquals(15, product.getStock());

        // 削除
        verify(stockDetailRepository).delete(sd);

        // 保存
        verify(productRepository).save(product);
    }

    // 異常系 StockDetailが見つからない
    @Test
    void deleteAndUpdateProductStock_shouldThrowExceptionWhenStockDetailNotFound() {

        // 準備
        when(stockDetailRepository.findById(10)).thenReturn(Optional.empty());

        // 実行
        assertThrows(RuntimeException.class, () -> stockDetailService.deleteAndUpdateProductStock(10));

        // 検証
        // delete や save は呼ばれない
        verify(stockDetailRepository, never()).delete(any());
        verify(productRepository, never()).save(any());
    }

    // 正常系 在庫が０になる場合
    @Test
    void deleteAndUpdateProductStock_shouldSetStockZeroWhenSumIsNull() {

        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(20);

        StockDetail sd = new StockDetail();
        sd.setId(10);
        sd.setProduct(product);

        when(stockDetailRepository.findById(10)).thenReturn(Optional.of(sd));

        // sumQuantity が null を返す場合
        when(stockDetailRepository.sumQuantityByProductId(1)).thenReturn(null);

        //実行
        stockDetailService.deleteAndUpdateProductStock(10);

        //検証
        assertEquals(0, product.getStock());
        verify(productRepository).save(product);
    }
}
