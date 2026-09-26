package com.example1.demo3.unit.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.controller.api.ProductMasterApiController;
import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductMasterService;

@ExtendWith(MockitoExtension.class)
class ProductMasterApiControllerTest {

    @Mock
    private ProductMasterService productMasterService;

    @InjectMocks
    private ProductMasterApiController productMasterApiController;

    @Test
    //正常系　一覧取得
    void list_shouldReturnAllProducts() {
        Product product = new Product();
        product.setId(1);
        product.setName("レタス");
        List<Product> expected = List.of(product);
        when(productMasterService.findAll()).thenReturn(expected);

        List<Product> actual = productMasterApiController.list();

        assertSame(expected, actual);
        assertEquals(product, actual.get(0));
        verify(productMasterService).findAll();
    }

    @Test
    //境界値　一覧取得　商品が存在しない場合
    void list_shouldReturnEmptyListWhenNoProductsExist() {
        List<Product> expected = List.of();
        when(productMasterService.findAll()).thenReturn(expected);

        List<Product> actual = productMasterApiController.list();

        assertSame(expected, actual);
        assertEquals(0, actual.size());
        verify(productMasterService).findAll();
    }

    @Test
    //正常系　商品1件取得
    void get_shouldReturnProductById() {
        Integer productId = 1;
        Product expected = new Product();
        expected.setId(productId);
        when(productMasterService.findById(productId)).thenReturn(expected);

        Product actual = productMasterApiController.get(productId);

        assertSame(expected, actual);
        verify(productMasterService).findById(productId);
    }

    @Test
    //例外系　商品が存在しない場合
    void get_shouldPropagateServiceExceptionForUnknownProduct() {
        Integer productId = Integer.MIN_VALUE;
        RuntimeException expected = new RuntimeException("product not found");
        when(productMasterService.findById(productId)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> productMasterApiController.get(productId));

        assertSame(expected, actual);
        verify(productMasterService).findById(productId);
    }

    @Test
    //正常系　商品登録
    void create_shouldReturnSavedProduct() {
        Product product = new Product();
        product.setName("レタス");
        when(productMasterService.save(product)).thenReturn(product);

        Product actual = productMasterApiController.create(product);

        assertSame(product, actual);
        verify(productMasterService).save(product);
    }

    @Test
    //例外系　商品登録
    void create_shouldPropagateServiceExceptionForInvalidProduct() {
        Product product = new Product();
        RuntimeException expected = new IllegalArgumentException("product is invalid");
        when(productMasterService.save(product)).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> productMasterApiController.create(product));

        assertSame(expected, actual);
        verify(productMasterService).save(product);
    }

    @Test
    //正常系　商品更新
    void update_shouldSetPathIdAndReturnSavedProduct() {
        Integer productId = Integer.MIN_VALUE;
        Product product = new Product();
        product.setId(999);
        when(productMasterService.save(product)).thenReturn(product);

        Product actual = productMasterApiController.update(productId, product);

        assertEquals(productId, product.getId());
        assertSame(product, actual);
        verify(productMasterService).save(product);
    }

    @Test
    //正常系　商品削除
    void delete_shouldDeleteProductById() {
        Integer productId = 1;

        productMasterApiController.delete(productId);

        verify(productMasterService).delete(productId);
    }

    @Test
    //例外系　商品削除
    void delete_shouldPropagateServiceException() {
        Integer productId = 1;
        doThrow(new RuntimeException("delete failed"))
                .when(productMasterService).delete(productId);

        assertThrows(RuntimeException.class,
                () -> productMasterApiController.delete(productId));
        verify(productMasterService).delete(productId);
    }
}