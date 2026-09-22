package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example1.demo3.controller.HistoryController;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.service.StockHistoryService;

@ExtendWith(MockitoExtension.class)
class HistoryControllerTest {

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private HistoryController historyController;

    @Test
    //正常値　入庫・出庫履歴をモデルへ設定しhistoryを返す
    void historyPage_shouldAddInAndOutHistoriesAndReturnHistoryView() {
        List<StockHistory> inHistory = List.of(new StockHistory());
        List<StockHistory> outHistory = List.of(new StockHistory(), new StockHistory());
        Model model = new ExtendedModelMap();

        when(stockHistoryService.searchAndSortIn(null, "dateDesc")).thenReturn(inHistory);
        when(stockHistoryService.searchAndSortOut(null, "dateDesc")).thenReturn(outHistory);

        String viewName = historyController.historyPage(model);

        assertEquals("history", viewName);
        assertSame(inHistory, model.getAttribute("inHistory"));
        assertSame(outHistory, model.getAttribute("outHistory"));
        verify(stockHistoryService).searchAndSortIn(null, "dateDesc");
        verify(stockHistoryService).searchAndSortOut(null, "dateDesc");
    }

    @Test
    //境界値　入庫・出庫履歴が空の場合、モデルへ空リストを設定しhistoryを返す
    void historyPage_shouldKeepEmptyHistoriesAsBoundaryValues() {
        List<StockHistory> emptyInHistory = List.of();
        List<StockHistory> emptyOutHistory = List.of();
        Model model = new ExtendedModelMap();

        when(stockHistoryService.searchAndSortIn(null, "dateDesc")).thenReturn(emptyInHistory);
        when(stockHistoryService.searchAndSortOut(null, "dateDesc")).thenReturn(emptyOutHistory);

        String viewName = historyController.historyPage(model);

        assertEquals("history", viewName);
        assertSame(emptyInHistory, model.getAttribute("inHistory"));
        assertSame(emptyOutHistory, model.getAttribute("outHistory"));
    }

    @Test
    //異常値　サービス層で例外が発生した場合、それを伝播する
    void historyPage_shouldPropagateServiceException() {
        Model model = new ExtendedModelMap();
        RuntimeException expected = new RuntimeException("history lookup failed");
        when(stockHistoryService.searchAndSortIn(null, "dateDesc")).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> historyController.historyPage(model));

        assertSame(expected, actual);
    }
}