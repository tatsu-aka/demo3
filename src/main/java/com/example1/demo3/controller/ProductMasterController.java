package com.example1.demo3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example1.demo3.entity.Product;
import com.example1.demo3.service.ProductService;

@Controller
@RequestMapping("/products/master")
public class ProductMasterController {
    @Autowired
    private ProductService productService;

    @Autowired
    private MakerService makerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "product-master-list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("makers", makerService.findAll());
        return "product-master-form";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute Product product) {
        productService.save(product);
        return "redirect:/products/master";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("makers", makerService.findAll());
        return "product-master-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productService.delete(id);
        return "redirect:/products/master";
    }
}
