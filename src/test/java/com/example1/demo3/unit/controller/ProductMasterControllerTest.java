package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example1.demo3.controller.ProductMasterController;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.service.ProductMasterService;

@ExtendWith(MockitoExtension.class)
class ProductMasterControllerTest {

    @Mock
    private ProductMasterService productMasterService;

    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private ProductMasterController productMasterController;

    @Test
    void productMasterPage_shouldReturnProductMasterView() {
        String viewName = productMasterController.productMasterPage();

        assertEquals("product-master", viewName);
    }

    @Test
    void newProduct_shouldInitializeProductFormWithMakers() {
        List<Maker> makers = List.of(new Maker());
        Model model = new ExtendedModelMap();
        when(makerRepository.findAll()).thenReturn(makers);

        String viewName = productMasterController.newProduct(model);

        assertEquals("product-master", viewName);
        assertInstanceOf(Product.class, model.getAttribute("product"));
        assertEquals(makers, model.getAttribute("makers"));
        verify(makerRepository).findAll();
    }

    @Test
    void newProduct_shouldHandleEmptyMakerListAtBoundary() {
        Model model = new ExtendedModelMap();
        when(makerRepository.findAll()).thenReturn(Collections.emptyList());

        String viewName = productMasterController.newProduct(model);

        assertEquals("product-master", viewName);
        assertEquals(Collections.emptyList(), model.getAttribute("makers"));
        verify(makerRepository).findAll();
    }

    @Test
    void save_shouldSaveProductAndRedirectToProductMaster() {
        Product product = new Product();

        String viewName = productMasterController.save(product);

        assertEquals("redirect:/products/master", viewName);
        verify(productMasterService).save(product);
    }

    @Test
    void save_shouldPropagateServiceExceptionForInvalidProduct() {
        Product product = new Product();
        when(productMasterService.save(product))
            .thenThrow(new IllegalArgumentException("product is invalid"));

        assertThrows(IllegalArgumentException.class, () -> productMasterController.save(product));
        verify(productMasterService).save(product);
    }

    @Test
    void edit_shouldAddProductAndMakersToModel() {
        Integer productId = 1;
        Product product = new Product();
        List<Maker> makers = List.of(new Maker());
        Model model = new ExtendedModelMap();
        when(productMasterService.findById(productId)).thenReturn(product);
        when(makerRepository.findAll()).thenReturn(makers);

        String viewName = productMasterController.edit(productId, model);

        assertEquals("product-master-form", viewName);
        assertEquals(product, model.getAttribute("product"));
        assertEquals(makers, model.getAttribute("makers"));
        verify(productMasterService).findById(productId);
        verify(makerRepository).findAll();
    }

    @Test
    void edit_shouldPropagateServiceExceptionForUnknownProduct() {
        Integer productId = Integer.MIN_VALUE;
        when(productMasterService.findById(productId))
            .thenThrow(new IllegalArgumentException("product not found"));

        assertThrows(IllegalArgumentException.class,
            () -> productMasterController.edit(productId, new ExtendedModelMap()));
        verify(productMasterService).findById(productId);
    }

    @Test
    void delete_shouldDeleteProductAtBoundaryIdAndRedirect() {
        Integer productId = Integer.MIN_VALUE;

        String viewName = productMasterController.delete(productId);

        assertEquals("redirect:/products/master", viewName);
        verify(productMasterService).delete(productId);
    }

    @Test
    void delete_shouldPropagateServiceExceptionForUnknownProduct() {
        Integer productId = 999;
        doThrow(new IllegalArgumentException("product not found"))
            .when(productMasterService).delete(productId);

        assertThrows(IllegalArgumentException.class,
            () -> productMasterController.delete(productId));
        verify(productMasterService).delete(productId);
    }

    @Test
    void newProduct_shouldPropagateMakerRepositoryException() {
        when(makerRepository.findAll())
            .thenThrow(new IllegalStateException("maker repository is unavailable"));

        assertThrows(IllegalStateException.class,
            () -> productMasterController.newProduct(new ExtendedModelMap()));
        verify(makerRepository).findAll();
    }
}
