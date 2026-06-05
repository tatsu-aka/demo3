package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductMasterService;

@RestController
@RequestMapping("/api/products/master")
public class ProductMasterApiController {

    private final ProductMasterService productMasterService;

    public ProductMasterApiController(ProductMasterService productMasterService) {
        this.productMasterService = productMasterService;
    }
    //一覧取得
    @GetMapping
    public List<Product> list() { return productMasterService.findAll(); }

    //一件取得
    @GetMapping("/{id}")
    public Product get(@PathVariable Integer id) { return productMasterService.findById(id); }
    
    //新規登録
    @PostMapping
    public Product create(@RequestBody Product product) { return productMasterService.save(product); }
    
    //更新
    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id, @RequestBody Product product) {
        product.setId(id);
        return productMasterService.save(product);
    }

    //削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { productMasterService.delete(id); }
}
