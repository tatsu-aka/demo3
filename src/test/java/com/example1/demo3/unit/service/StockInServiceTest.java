package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import com.example1.demo3.service.StockInService;

@ExtendWith(MockitoExtension.class)
public class StockInServiceTest {
    
    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private MakerRepository makerRepository;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @InjectMocks
    private StockInService stockInService;

    //正常系
    @Test
    void inStock_shouldUpdateExistingStockDetail() {
        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(10);

        Maker maker = new Maker();
        maker.setId(2);

        StockDetail detail = new StockDetail();
        detail.setProduct(product);
        detail.setQuantity(5);
        detail.setMaker(maker);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(makerRepository.findById(2)).thenReturn(Optional.of(maker));
        when(stockDetailRepository.findByProductIdAndMakerId(1, 2)).thenReturn(Optional.of(detail));

        //実行
        stockInService.inStock(1, 3, 2, "個", "野菜");

        //検証
        assertEquals(13, product.getStock());
        assertEquals(8, detail.getQuantity());

        verify(productRepository).save(product);
        verify(stockDetailRepository).save(detail);
        verify(stockHistoryRepository).save(any(StockHistory.class));
    }

    @Test
    void inStock_shouldCreateNewStockDetailWhenNotExists() {
        //準備
        Product product = new Product();
        product.setId(1);
        product.setStock(10);

        Maker maker = new Maker();
        maker.setId(2);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(makerRepository.findById(2)).thenReturn(Optional.of(maker));
        when(stockDetailRepository.findByProductIdAndMakerId(1, 2)).thenReturn(Optional.empty());

        //実行
        stockInService.inStock(1, 5, 2, "箱", "野菜");

        //product 更新確認
        assertEquals(15, product.getStock());

        //saveされているか確認
        verify(stockDetailRepository).save(argThat(d ->
            d.getProduct() == product &&
            d.getMaker() == maker &&
            d.getQuantity() == 5
        ));
        verify(stockHistoryRepository).save(any(StockHistory.class));
    }

    //異常系　商品が見つからない
    @Test
    void inStock_shouldThrowExceptionWhenProductNotFound() {
        //準備
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        //実行
        assertThrows(IllegalArgumentException.class,
            () -> stockInService.inStock(1, 3, 2, "個", "野菜"));

        //検証
        verify(productRepository).findById(1);
    }

    //異常系　メーカーが見つからない
    @Test
    void inStock_shouldThorwExceptionWhenMakerNotFound() {

        //準備
        Product product = new Product();
        product.setId(1);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(makerRepository.findById(2)).thenReturn(Optional.empty());

        //実行
        assertThrows(IllegalArgumentException.class,
            () ->stockInService.inStock(1, 3, 2, "個", "野菜"));

        //検証
        verify(makerRepository).findById(2);

    }

}
