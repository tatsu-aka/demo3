package com.example1.demo3.controller;

import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.ProductService;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class ProductController {

    private final StockHistoryRepository stockHistoryRepository;

    private final ProductRepository productRepository;
    
    private final ProductService productService;
    
    public ProductController(ProductService productService, ProductRepository productRepository, StockHistoryRepository stockHistoryRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    @GetMapping("/products")
    public String listProduct(Model model) {
        var products = productService.findAllProducts();
        model.addAttribute("products", products);
        return "product-list";
    }
    
    @GetMapping("/test-save")
    public String testSave() {
        Product p = new Product();
        p.setName("リンゴ");
        p.setStock(10);
        p.setCostPrice(500);
        p.setSalePrice(800);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        productService.save(p);

        return "redirect:/products";
        
    }
    
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-new";
    }

    @PostMapping("/products/new")
    public String createProduct(@RequestParam String name, @RequestParam Integer stock,
        @RequestParam Integer costPrice, @RequestParam Integer salePrice) {
        Product product = new Product();
        product.setName(name);
        product.setStock(stock);
        product.setCostPrice(costPrice);
        product.setSalePrice(salePrice);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productService.save(product);
        return "redirect:/products";
    }
    
    
    
    @PostMapping("/products")
    public String createProduct(@ModelAttribute Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productService.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

    @GetMapping("/products/out/{id}")
    public String showOutForm(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product-out";
    }

    @PostMapping("/products/out/{id}")
    public String outProduct(@PathVariable Integer id, @RequestParam Integer quantity) {
        productService.outStock(id, quantity);
        return "redirect:/products";
    }

    @GetMapping("/products/in/{id}")
    public String showInForm(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product-in";
    }

    @PostMapping("/products/in/{id}")
    public String inProduct(@PathVariable Integer id, @RequestParam Integer quantity) {
        productService.inStock(id, quantity);
        return "redirect:/products";
    }
}
