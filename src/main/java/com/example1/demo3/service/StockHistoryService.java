package com.example1.demo3.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockHistoryService;

@Service
public class StockHistoryService {
    
    private final StockHistoryRepository stockHistoryRepository;

    public StockHistoryService(StockHistoryRepository stockHistoryRepository) {
        this.stockHistoryRepository = stockHistoryRepository;
    }

    //出庫履歴検索・ソート
    public List<StockHistory> searchAndSortOut(String keyword, String sort) {
        List<StockHistory> list = stockHistoryRepository.searchOut(keyword);
        return sortList(list, sort);
    }        
    //入庫履歴検索・ソート
    public List<StockHistory> searchAndSortIn(String keyword, String sort) {
        List<StockHistory> list = stockHistoryRepository.searchIn(keyword);
        return sortList(list, sort);
    }
    //共通ソート
    private List<StockHistory> sortList(List<StockHistory> list, String sort) {
        switch (sort) {
            case "dateAsc":
                list.sort(Comparator.comparing(StockHistory::getDateTime));
                break;
            case "qtyDesc":
                list.sort(Comparator.comparing(StockHistory::getQuantity).reversed());
                break;
            case "qtyAsc":
                list.sort(Comparator.comparing(StockHistory::getQuantity));
                break;
            default:
                list.sort(Comparator.comparing(StockHistory::getDateTime).reversed());
        }    
        return list;    
    }
}    