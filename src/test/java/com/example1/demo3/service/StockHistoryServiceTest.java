package com.example1.demo3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.dto.StockHistoryDto;
import com.example1.demo3.dto.StockHistoryInDto;
import com.example1.demo3.dto.StockHistoryOutDto;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.StockHistoryRepository;

@ExtendWith(MockitoExtension.class)
public class StockHistoryServiceTest {

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    // 出庫履歴検索
    @Test
    void searchAndSortOut_qtyAsc() {

        // 準備
        StockHistory h1 = new StockHistory();
        h1.setQuantity(10);
        StockHistory h2 = new StockHistory();
        h2.setQuantity(5);

        when(stockHistoryRepository.searchOut("野菜")).thenReturn(Arrays.asList(h1, h2));

        // 実行
        List<StockHistory> result = stockHistoryService.searchAndSortOut("野菜", "qtyAsc");

        // 検証
        assertEquals(5, result.get(0).getQuantity());
        assertEquals(10, result.get(1).getQuantity());
    }

    // 入庫履歴検索
    @Test
    void searchAndSortIn_dateDesc() {

        //準備
        StockHistory h1 = new StockHistory();
        h1.setDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        StockHistory h2 = new StockHistory();
        h2.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));

        when(stockHistoryRepository.searchIn("野菜")).thenReturn(Arrays.asList(h1, h2));

        // 実行
        List<StockHistory> result = stockHistoryService.searchAndSortIn("野菜", "dateDesc");

        // 検証
        assertEquals(h2, result.get(0));
        assertEquals(h1, result.get(1));
    }

    // ソート デフォルト
    @Test
    void sortList_defaultDateDesc() {
        
        //準備
        StockHistory h1 = new StockHistory();
        h1.setDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        StockHistory h2 = new StockHistory();
        h2.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));

        when(stockHistoryRepository.searchOut("食品")).thenReturn(Arrays.asList(h1, h2));

        //実行
        List<StockHistory> result = stockHistoryService.searchAndSortOut("食品", null);

        //検証
        assertEquals(h2, result.get(0));
        assertEquals(h1, result.get(1));
    }

    // グラフ用
    @Test
    void getStockHistoryForChart() {

        //準備
        StockHistory h1 = new StockHistory();
        h1.setDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        h1.setStock(50);

        StockHistory h2 = new StockHistory();
        h2.setDateTime(LocalDateTime.of(2024, 1, 2, 10, 0));
        h2.setStock(60);

        when(stockHistoryRepository.findByProductIdOrderByDateTimeAsc(1)).thenReturn(Arrays.asList(h1, h2));

        //実行
        List<StockHistoryDto> result = stockHistoryService.getStockHistoryForChart(1);

        //検証
        assertEquals(50, result.get(0).getStock());
        assertEquals(60, result.get(1).getStock());
    }

    //入庫グラフ用
    @Test
    void getInHistory() {

        //準備
        StockHistory h1 = new StockHistory();
        h1.setDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        h1.setQuantity(5);

        when(stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(1L, "IN"))
                .thenReturn(Arrays.asList(h1));

        //実行
        List<StockHistoryInDto> result = stockHistoryService.getInHistory(1L);

        //検証
        assertEquals(5, result.get(0).getQuantity());
    }

    //出庫グラフ用
    @Test
    void getOutHistory() {

        //準備
        StockHistory h1 = new StockHistory();
        h1.setDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        h1.setQuantity(3);

        when(stockHistoryRepository.findByProductIdAndTypeOrderByDateTimeAsc(1L, "OUT"))
                .thenReturn(Arrays.asList(h1));

        //実行
        List<StockHistoryOutDto> result = stockHistoryService.getOutHistory(1L);

        //検証
        assertEquals(3, result.get(0).getQuantity());
    }
}
