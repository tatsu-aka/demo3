package com.example1.demo3.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;

@RestController
@RequestMapping("/api/makers")
public class MakerApiController {

    private final MakerRepository makerRepository;

    public MakerApiController(MakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    @GetMapping
    public List<Maker> getMakers() {
        return makerRepository.findAll();
    }
}
