package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.example1.demo3.controller.ProductController;
import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;



@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void listProducts_shouldReturnProductList() {
        //準備
        Product product = new Product();
        product.setId(1);
        product.setName("レタス");

        List<Product> products = List.of(product);
        Model model = new ExtendedModelMap();

        when(productService.search("野菜")).thenReturn(products);

        //実行
        String viewName = productController.listProducts("野菜", model);

        //検証
        assertEquals("product-list", viewName);
        assertEquals(products, model.getAttribute("products"));
        assertEquals("野菜", model.getAttribute("keyword"));
        verify(productService).search("野菜");
    }

    @Test
    void showCreateForm_shouldReturnProductNewView() {
        //準備
        Model model = new ExtendedModelMap();

        //実行
        String viewName = productController.showCreateForm(model);

        //検証
        assertEquals("product-new", viewName);
        
        Object actualProduct = model.getAttribute("product");
        assertNotNull(actualProduct);
        assertInstanceOf(Product.class, actualProduct);
        assertEquals(List.of("野菜", "果物"), model.getAttribute("categories"));
    }

    @Test
    void createProduct_shouldSaveProductAndRedirectToProductList() {
        //準備
        Product product = new Product();
        product.setName("レタス");

        //実行
        String viewName = productController.createProduct(product);

        //検証
        assertEquals("redirect:/product", viewName);
        verify(productService).save(product);
    }

    @Test
    void editProduct_shouldAddProductToModelAndReturnEditView() {
        //準備
        int productId = 1;
        Product product = new Product();
        product.setId(productId);
        product.setName("レタス");
        Model model = new ExtendedModelMap();

        when(productService.findById(productId)).thenReturn(product);

        //実行
        String viewName = productController.editProduct(productId, model);

        //検証
        assertEquals("product-edit", viewName);
        assertEquals(product, model.getAttribute("product"));
        verify(productService).findById(productId);
    }

    @Test
    void updateProduct_shouldSetPathIdSaveProductAndRedirectToProductList() {
        //準備
        int productId = 1;
        Product product = new Product();

        //実行
        String viewName = productController.updateProduct(productId, product);

        //検証
        assertEquals("redirect:/product", viewName);
        assertEquals(productId, product.getId());
        verify(productService).save(product);
    }

    @Test
    void getProducts_shouldMapProductsIncludingMakerName() {
        //準備
        Maker maker = new Maker();
        maker.setName("青果メーカー");

        Product productWithMaker = new Product();
        productWithMaker.setId(1);
        productWithMaker.setName("レタス");
        productWithMaker.setCategory("野菜");
        productWithMaker.setUnit("個");
        productWithMaker.setStock(10);
        productWithMaker.setMaker(maker);

        Product productWithoutMaker = new Product();
        productWithoutMaker.setId(2);
        productWithoutMaker.setName("りんご");
        productWithoutMaker.setCategory("果物");
        productWithoutMaker.setUnit("袋");
        productWithoutMaker.setStock(5);

        when(productService.findAll()).thenReturn(List.of(productWithMaker, productWithoutMaker));

        //実行
        List<ProductDto> result = productController.getProducts();

        //検証
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("レタス", result.get(0).getName());
        assertEquals("野菜", result.get(0).getCategory());
        assertEquals("個", result.get(0).getUnit());
        assertEquals(10, result.get(0).getStock());
        assertEquals("青果メーカー", result.get(0).getMakerName());
        assertEquals(2, result.get(1).getId());
        assertNull(result.get(1).getMakerName());
        verify(productService).findAll();
    }
}
