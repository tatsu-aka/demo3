package com.example1.demo3.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example1.demo3.dto.StockHistoryDto;
import com.example1.demo3.dto.StockHistoryInDto;
import com.example1.demo3.dto.StockHistoryOutDto;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.StockHistoryRepository;
import com.example1.demo3.service.StockHistoryService;

@Service
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

    public StockHistoryService(StockHistoryRepository stockHistoryRepository) {
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // 出庫履歴検索・ソート
    public List<StockHistory> searchAndSortOut(String keyword, String sort) {
        List<StockHistory> list = stockHistoryRepository.searchOut(keyword);
        return sortList(list, sort);
    }

    // 入庫履歴検索・ソート
    public List<StockHistory> searchAndSortIn(String keyword, String sort) {
        List<StockHistory> list = stockHistoryRepository.searchIn(keyword);
        return sortList(list, sort);
    }

    // 共通ソート処理
    private List<StockHistory> sortList(List<StockHistory> list, String sort) {

        if (sort == null || sort.isEmpty()) {
            sort = "dateDesc"; // デフォルト
        }

        switch (sort) {
            case "dateAsc":
                Comparator<StockHistory> dateComparator = Comparator.comparing(StockHistory::getDateTime, Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "dateDesc":
                list.sort(Comparator.comparing(StockHistory::getDateTime).reversed());
                break;

            case "qtyAsc":
                list.sort(Comparator.comparing(StockHistory::getQuantity));
                break;

            case "qtyDesc":
                list.sort(Comparator.comparing(StockHistory::getQuantity).reversed());
                break;

            default:
                // 想定外の値が来ても安全に動く
                list.sort(Comparator.comparing(StockHistory::getDateTime).reversed());
                break;
        }

        return list;
    }

    //グラフ用：指定商品の履歴を昇順
    public List<StockHistoryDto> getStockHistoryForChart(Integer productId) {
        List<StockHistory> list = stockHistoryRepository.findByProductIdOrderByDateTimeAsc(productId);
        return list.stream()
        .map(h -> new StockHistoryDto(h.getDateTime(), h.getStock())).toList();
    }
    //入庫グラフ用
    public List<StockHistoryInDto> getInHistory(Long productId) {
        return stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(productId, "IN").stream()
        .map(h -> new StockHistoryInDto(h.getDateTime(), h.getQuantity())).toList();
    }
    //出庫グラフ用
    public List<StockHistoryOutDto> getOutHistory(Long productId) {
        return stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(productId, "OUT").stream()
        .map(h -> new StockHistoryOutDto(h.getDateTime(), h.getQuantity())).toList();
    }
}