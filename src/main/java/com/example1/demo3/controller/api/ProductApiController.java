package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.dto.ProductRequest;
import com.example1.demo3.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    //商品一覧　（出力DTO）
    @GetMapping
    public List<ProductDto> getProducts() {
        return productService.findAllDto();
    }
    
    //商品登録　（入力DTO＋バリデーション）
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest req, BindingResult result) {
        if (result.hasErrors()) {
            List<String> messages = result.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(messages);
        }

        productService.create(req);
        return ResponseEntity.ok("ok");
    }

    //商品更新
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
        @PathVariable Integer id,
        @Valid @RequestBody ProductRequest req,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            List<String> messages = result.getAllErrors().stream()
            .map(ObjectError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(messages);
        }
        productService.update(id, req);
        return ResponseEntity.ok("ok");
    }

    //商品削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
