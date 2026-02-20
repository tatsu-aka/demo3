package com.example1.demo3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;


@Controller
@RequestMapping("/makers")
public class MakerController {

    private final MakerRepository makerRepository;

    public MakerController(MakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("makers", makerRepository.findAll());
        model.addAttribute("maker", new Maker());
        return "maker-list";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Maker maker) {
        makerRepository.save(maker);
        return "redirect:/makers";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        makerRepository.deleteById(id);
        return "redirect:/makers";
    }
}
