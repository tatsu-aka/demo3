package com.example1.demo3.controller;

import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
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
        return "redirect:/products";
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
        return "redirect:/products";
    }

    // 削除
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

    // 出庫フォーム
    @GetMapping("/out")
    public String showOutForm(Model model) {
        model.addAttribute("products", productService.findAll());
        return "product-out";
    }

    // 出庫処理
    @PostMapping("/out")
    public String outProduct(@RequestParam Integer productId, @RequestParam Integer quantity) {
        productService.outStock(productId, quantity);
        return "redirect:/products";
    }

    // 入庫フォーム
    @GetMapping("/in")
    public String showInForm(Model model) {
        model.addAttribute("products", productService.findAll());
        return "product-in";
    }

    // 入庫処理
    @PostMapping("/in")
    public String inProduct(@RequestParam Integer productId, @RequestParam Integer quantity,
        @RequestParam String maker, @RequestParam String unit
    ) {
        productService.inStock(productId, quantity, maker, unit);
        return "redirect:/products";
    }
}