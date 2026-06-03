package com.example1.demo3.controller;

import com.example1.demo3.dto.ProductDto;
import com.example1.demo3.entity.Product;
import com.example1.demo3.service.MakerService;
import com.example1.demo3.service.ProductService;
import com.example1.demo3.service.StockInService;
import com.example1.demo3.service.StockOutService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    

    public ProductController(ProductService productService) {
        this.productService = productService;
        
    }

    // 商品一覧 + 検索
    @GetMapping
    public String listProducts(@RequestParam(required = false) String keyword, Model model) {
        List<Product> products = productService.search(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "product-list";
    }

    // 新規登録フォーム
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        //カテゴリ
        model.addAttribute("categories", List.of("野菜", "果物"));
        return "product-new";
    }

    // 新規登録（保存）
    @PostMapping("/new")
    public String createProduct(@ModelAttribute Product product) {
        productService.save(product);
        return "redirect:/product";
    }

    // 編集フォーム
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product-edit";
    }

    // 編集保存
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Integer id, @ModelAttribute Product product) {
        product.setId(id);
        productService.save(product);
        return "redirect:/product";
    }

    //商品一覧API
    @GetMapping("/api/products")
    @ResponseBody
    public List<ProductDto> getProducts() {
        return productService.findAll().stream().map(p -> new ProductDto(
            p.getId(), p.getName(), p.getCategory(), p.getUnit(), p.getStock(), p.getMaker() != null ? p.getMaker().getName() : null)).toList();
    }

    
}