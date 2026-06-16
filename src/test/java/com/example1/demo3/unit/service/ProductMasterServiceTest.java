package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.service.ProductMasterService;

@ExtendWith(MockitoExtension.class)
public class ProductMasterServiceTest {
    
    @Mock
    private ProductRepository productRepository;

    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private ProductMasterService productMasterService;

    @Test
    void findAll_shouldReturnAllProducts() {
        //準備
        List<Product> products = List.of(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(products);

        //実行
        List<Product> result = productMasterService.findAll();

        //検証
        assertEquals(products, result);
        verify(productRepository).findAll();
        
    }
    @Test
    void findById_shouldReturnProduct() {
        //準備
        Product product = new Product();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        //実行
        Product result = productMasterService.findById(1);

        //検証
        assertEquals(product, result);
        verify(productRepository).findById(1);

    }
    //save 新規登録
    @Test
    void save_shouldSaveNewProductWithMaker() {
        Product product = new Product();
        Maker maker = new Maker();
        maker.setId(10);
        product.setMaker(maker);

        Maker foundMaker = new Maker();
        foundMaker.setId(10);

        when(makerRepository.findById(10)).thenReturn(Optional.of(foundMaker));
        when(productRepository.save(product)).thenReturn(product);

        //実行
        Product result = productMasterService.save(product);

        //検証
        assertEquals(foundMaker, result.getMaker());
        verify(makerRepository).findById(10);
        verify(productRepository).save(product);
    }
    //save 更新
    @Test
    void save_shouldKeepCreatedAtWhenUpdating() {
        //準備
        Product product = new Product();
        product.setId(1);

        Product existing = new Product();
        existing.setId(1);
        existing.setCreatedAt(LocalDateTime.now());

        when(productRepository.findById(1)).thenReturn(Optional.of(existing));
        when(productRepository.save(product)).thenReturn(product);

        //実行
        Product result = productMasterService.save(product);

        //検証
        assertEquals(existing.getCreatedAt(), result.getCreatedAt());
        verify(productRepository).findById(1);
        verify(productRepository).save(product);
    }
    //削除
    @Test
    void delete_shouldCallRepositoryDeleteById() {
        //実行
        productMasterService.delete(1);

        //検証
        verify(productRepository).deleteById(1);
    }

}
