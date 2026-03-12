package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.service.MakerService;

@RestController
@RequestMapping("/api/master")
public class MasterApiController {

    private final MakerService makerService;

    public MasterApiController(MakerService makerService) {
        this.makerService = makerService;
    }
    
    @GetMapping("/categories")
    public List<String> categories() {
        return List.of("野菜", "果物");
    }

    @GetMapping("/units")
    public List<String> units() {
        return List.of("個", "P", "kg", "ケース");
    }

    @GetMapping("/makers")
    public List<Maker> makers() {
        return makerService.findAll();
    }

}
