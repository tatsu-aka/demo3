package com.example1.demo3.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example1.demo3.entity.Product;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.service.ProductMasterService;




@Controller
@RequestMapping("/master")
public class ProductMasterController {
    private final ProductMasterService productMasterService;
    private final MakerRepository makerRepository;

    public ProductMasterController(ProductMasterService productMasterService,MakerRepository makerRepository) {
        this.productMasterService = productMasterService;
        this.makerRepository = makerRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productMasterService.findAll());
        return "product-master-list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("makers", makerRepository.findAll());
        return "product-master-form";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute Product product) {
        productMasterService.save(product);
        return "redirect:/products/master";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productMasterService.findById(id));
        model.addAttribute("makers", makerRepository.findAll());
        return "product-master-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productMasterService.delete(id);
        return "redirect:/products/master";
    }
}
