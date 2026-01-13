package com.example1.demo3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import com.example1.demo3.repository.StockHistoryRepository;

@Controller
public class HistoryController {
    
    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    //入庫履歴
    @GetMapping("/history/in")
    public String showInHistory(Model model) {
        model.addAttribute("histories",
            stockHistoryRepository.findByTypeOrderByDateTimeDesc("IN"));
        return "history-in";
    }
    
    //出庫履歴
    @GetMapping("/history/out")
    public String showOutHistory(Model model) {
        model.addAttribute("histories",
            stockHistoryRepository.findByTypeOrderByDateTimeDesc("OUT"));
        return "history-out";
    }

}
