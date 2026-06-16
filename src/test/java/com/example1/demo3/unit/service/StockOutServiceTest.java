package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockOutService;

@ExtendWith(MockitoExtension.class)
public class StockOutServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private StockOutService stockOutService;

    // 正常系
    @Test
    void outStock_shouldUpdateStockAndDetail() {

        // 準備
        Product product = new Product();
        product.setId(1);
        product.setStock(10);

        Maker maker = new Maker();
        maker.setId(2);

        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setMaker(maker);
        detail.setQuantity(7);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(makerRepository.findById(2)).thenReturn(Optional.of(maker));
        when(stockDetailRepository.findByProductIdAndMakerId(1, 2)).thenReturn(Optional.of(detail));

        // 実行
        stockOutService.outStock(1, 5, "個", "野菜", 2);

        // 検証
        assertEquals(5, product.getStock());
        assertEquals(2, detail.getQuantity());

        verify(productRepository).save(product);
        verify(stockDetailRepository).save(detail);
        verify(stockHistoryRepository).save(any(StockHistory.class));
    }

    // 異常系 商品がみつからない
    @Test
    void outStock_shouldThrowExceptionWhenProductNotFound() {

        // 準備
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // 実行
        assertThrows(IllegalArgumentException.class, () -> stockOutService.outStock(1, 5, "個", "野菜", 2));
    }

    //異常系　在庫不足
    @Test
    void outStock_shouldThrowExceptionWhenStockIsNotEnough() {

        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(3);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        //実行
        assertThrows(IllegalArgumentException.class, () -> stockOutService.outStock(1, 5, "個", "野菜", 2));

    }

    //異常系　内訳が見つからない
    @Test
    void outStock_shouldThrowExceptionWhenStockDetailNotFound() {

        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(stockDetailRepository.findByProductIdAndMakerId(1, 2)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> stockOutService.outStock(1, 5, "個", "野菜", 2));
    }

    //異常系　メーカーが見つからない
    @Test
    void outStock_shouldThrowExceptionWhenMakerNotFound() {

        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(10);

        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setQuantity(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(makerRepository.findById(2)).thenReturn(Optional.empty());
        when(stockDetailRepository.findByProductIdAndMakerId(1, 2)).thenReturn(Optional.of(detail));

        //実行
        assertThrows(IllegalArgumentException.class, () -> stockOutService.outStock(1, 5, "個", "野菜", 2));
    }
}
