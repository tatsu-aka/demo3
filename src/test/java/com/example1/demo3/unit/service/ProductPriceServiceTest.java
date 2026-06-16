package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.OpenOption;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.ProductPrice;
import com.example1.demo3.repository.ProductPriceRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.service.ProductPriceService;

@ExtendWith(MockitoExtension.class)
public class ProductPriceServiceTest {
    
    @Mock
    private ProductPriceRepository priceRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductPriceService priceService;

    //正常系
    @Test
    void changePrice_shouldCloseCurrentPriceAndNewPrice() {

        //準備
        Integer productId = 1;
        LocalDate newStartDate = LocalDate.of(2024, 1, 1);

        //現在の価格
        ProductPrice current = new ProductPrice();
        current.setCostPrice(100);
        current.setStartDate(LocalDate.of(2023, 1, 1));
        current.setEndDate(null);

        //商品
        Product product = new Product();
        product.setId(productId);

        when(priceRepository.getCurrentPrice(productId)).thenReturn(current);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        //実行
        priceService.changePrice(productId, 200, newStartDate);

        //検証
        //現在のendDateが閉じているか
        assertEquals(LocalDate.of(2023, 12, 31), current.getEndDate());

        //saveが2回呼ばれているか？
        verify(priceRepository, times(2)).save(any(ProductPrice.class));
    }

    //正常系　新価格追加
    @Test
    void changePrice_shouldAddNewPriceWhenNoCurrentPrice() {

        //準備
        Integer productId = 1;
        LocalDate newStartDate = LocalDate.of(2024, 1, 1);

        when(priceRepository.getCurrentPrice(productId)).thenReturn(null);

        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        //実行
        priceService.changePrice(productId, 300, newStartDate);

        //saveが１回
        verify(priceRepository, times(1)).save(any(ProductPrice.class));
    }

    //異常系　商品が見つからない
    @Test
    void changePrice_shouldThorwExceptionWhenProductNotFound() {

        //準備
        Integer productId = 1;

        when(priceRepository.getCurrentPrice(productId)).thenReturn(null);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        //実行
        assertThrows(IllegalArgumentException.class, () -> priceService.changePrice(productId, 200, LocalDate.now()));

        //saveは呼ばれない
        verify(priceRepository, never()).save(any());
    }
}
