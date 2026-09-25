package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.controller.api.ProductApiController;
import com.example1.demo3.dto.ProductMakerStockDto;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductApiControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @InjectMocks
    private ProductApiController productApiController;

    @Test
    void listByMaker_shouldReturnProductMakerStockList() {
        ProductMakerStockDto product = new ProductMakerStockDto(
                1, 10, "レタス", "メーカーA", 5, "個", "野菜");
        List<ProductMakerStockDto> expected = List.of(product);
        when(productService.findProductMakerStockList()).thenReturn(expected);

        List<ProductMakerStockDto> actual = productApiController.listByMaker();

        assertSame(expected, actual);
        assertEquals(product, actual.get(0));
        verify(productService).findProductMakerStockList();
    }

    @Test
    void listByMaker_shouldReturnEmptyListWhenNoProductsExist() {
        List<ProductMakerStockDto> expected = List.of();
        when(productService.findProductMakerStockList()).thenReturn(expected);

        List<ProductMakerStockDto> actual = productApiController.listByMaker();

        assertSame(expected, actual);
        assertEquals(0, actual.size());
        verify(productService).findProductMakerStockList();
    }

    @Test
    void listByMaker_shouldPropagateServiceException() {
        RuntimeException expected = new RuntimeException("product lookup failed");
        when(productService.findProductMakerStockList()).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> productApiController.listByMaker());

        assertSame(expected, actual);
        verify(productService).findProductMakerStockList();
    }
}