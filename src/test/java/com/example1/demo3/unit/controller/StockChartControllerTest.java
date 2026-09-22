package com.example1.demo3.unit.controller;

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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example1.demo3.controller.StockChartController;
import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;

@ExtendWith(MockitoExtension.class)
class StockChartControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private StockChartController stockChartController;

    @Test
    // 正常値: 商品一覧と商品名をモデルへ設定し、stock-chartを返す
    void stockChartPage_shouldAddProductsAndReturnStockChartView() {
        Product appleProduct = new Product();
        appleProduct.setName("リンゴ");

        Product lettuceProduct = new Product();
        lettuceProduct.setName("レタス");

        List<Product> products = List.of(appleProduct, lettuceProduct);
        Model model = new ExtendedModelMap();

        when(productService.findAll()).thenReturn(products);

        String viewName = stockChartController.stockChartPage(model);

        assertEquals("stock-chart", viewName);
        @SuppressWarnings("unchecked")
        List<Product> actualProducts = (List<Product>) model.getAttribute("products");
        assertEquals(2, actualProducts.size());
        assertEquals("リンゴ", actualProducts.get(0).getName());
        assertEquals("レタス", actualProducts.get(1).getName());
        assertSame(products, actualProducts);
        verify(productService).findAll();
    }

    @Test
    // 境界値: 商品一覧が空の場合でも画面を表示する
    void stockChartPage_shouldHandleEmptyProductList() {
        List<Product> emptyProducts = List.of();
        Model model = new ExtendedModelMap();

        when(productService.findAll()).thenReturn(emptyProducts);

        String viewName = stockChartController.stockChartPage(model);

        assertEquals("stock-chart", viewName);
        assertSame(emptyProducts, model.getAttribute("products"));
    }

    @Test
    // 異常値: 商品取得時の例外を呼び出し元へ伝播する
    void stockChartPage_shouldPropagateServiceException() {
        Model model = new ExtendedModelMap();
        RuntimeException expected = new RuntimeException("product lookup failed");

        when(productService.findAll()).thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> stockChartController.stockChartPage(model));

        assertSame(expected, actual);
    }
}