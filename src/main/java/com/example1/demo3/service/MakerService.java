package com.example1.demo3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;

@Service
public class MakerService {

    private final MakerRepository makerRepository;

    public MakerService(MakerRepository makerRepository) {
        this.makerRepository = makerRepository;
    }

    public List<Maker> findAll() {
        return makerRepository.findAll();
    }

    public Maker findById(Integer id) {
        return makerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("取引先が見つかりません"));
    }

    public void save(Maker maker) {
        makerRepository.save(maker);
    }

    public void delete(Integer id) {
        makerRepository.deleteById(id);
    }
}
