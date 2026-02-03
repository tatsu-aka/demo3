package com.example1.demo3.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockHistoryService;

@Controller
public class HistoryController {
    
    private final StockHistoryRepository stockHistoryRepository;
    private final StockHistoryService stockHistoryService;

    public HistoryController(StockHistoryRepository stockHistoryRepository, StockHistoryService stockHistoryService) {
        this.stockHistoryRepository = stockHistoryRepository;
        this.stockHistoryService = stockHistoryService;
    }

    @GetMapping("/history")
    public String historyPage(Model model) {

    // 入庫履歴
    List<StockHistory> inList = stockHistoryService.searchAndSortIn(null, "dateDesc");

    // 出庫履歴
    List<StockHistory> outList = stockHistoryService.searchAndSortOut(null, "dateDesc");

    model.addAttribute("inHistory", inList);
    model.addAttribute("outHistory", outList);

    return "history";  // ← タブ画面のHTML
    }

}
