package com.example1.demo3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.dto.ProductRequest;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private MakerRepository makerRepository;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_shouldSaveProductWithMakerAndInitialStockZero() {
        //準備
        ProductRequest req = new ProductRequest();
        req.setName("レタス");
        req.setCategory("野菜");
        req.setUnit("個");
        req.setCostPrice(100);
        req.setMakerId(1);

        Maker maker = new Maker();
        maker.setId(1);
        maker.setName("maker");

        when(makerRepository.findById(1)).thenReturn(Optional.of(maker));

        //実行
        productService.create(req);

        //検証
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product saved = captor.getValue();
        assertEquals("レタス", saved.getName());
        assertEquals("野菜", saved.getCategory());
        assertEquals("個", saved.getUnit());
        assertEquals(100, saved.getCostPrice());
        assertEquals(0, saved.getStock());
        assertEquals(maker, saved.getMaker());
    }

    @Test
    void update_shouldApplyRequestAndSavedUpdateProduct() {
        //準備
        int productId = 1;

        ProductRequest req = new ProductRequest();
        req.setName("キャベツ");
        req.setCategory("野菜");
        req.setUnit("個");
        req.setCostPrice(120);
        req.setMakerId(2);
        
        //既存のProduct
        Product existing = new Product();
        existing.setId(productId);
        existing.setName("レタス");
        existing.setCategory("野菜");
        existing.setUnit("個");
        existing.setCostPrice(100);

        //新しいMaker
        Maker maker = new Maker();
        maker.setId(2);
        maker.setName("新しいメーカー");

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(makerRepository.findById(2)).thenReturn(Optional.of(maker));

        //実行
        productService.update(productId, req);

        //検証
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        Product saved = captor.getValue();

        //更新された値の検証
        assertEquals("キャベツ", saved.getName());
        assertEquals("野菜", saved.getCategory());
        assertEquals("個", saved.getUnit());
        assertEquals(120, saved.getCostPrice());
        assertEquals(maker, saved.getMaker());

    }

    @Test
    void deleteProduct_shouldClearRelationsAndDeleteProduct() {

        //準備
        int productId = 1;

        Product existing = new Product();
        existing.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        //実行
        productService.deleteProduct(productId);

        //検証
        verify(stockDetailRepository).clearProductId(productId);
        verify(stockHistoryRepository).clearProductId(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void search_shouldReturnAllProductsWhenKeywordIsNullOrEmpty() {
        //準備
        List<Product> allProducts = List.of(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(allProducts);

        //実行
        List<Product> result1 = productService.search(null);
        List<Product> result2 = productService.search("");

        //検証
        verify(productRepository, times(2)).findAll();

        assertEquals(allProducts, result1);
        assertEquals(allProducts, result2);
    }

}
