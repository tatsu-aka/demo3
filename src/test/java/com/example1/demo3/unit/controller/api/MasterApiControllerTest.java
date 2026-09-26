package com.example1.demo3.unit.controller.api;

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

import com.example1.demo3.controller.api.MasterApiController;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.service.MakerService;

@ExtendWith(MockitoExtension.class)
class MasterApiControllerTest {

    @Mock
    private MakerService makerService;

    @InjectMocks
    private MasterApiController masterApiController;

    @Test
    //正常系　カテゴリ一覧取得
    void categories_shouldReturnSupportedCategories() {
        List<String> actual = masterApiController.categories();

        assertEquals(List.of("野菜", "果物"), actual);
    }

    @Test
    //正常系　単位一覧取得
    void units_shouldReturnSupportedUnitsInOrder() {
        List<String> actual = masterApiController.units();

        assertEquals(List.of("個", "P", "kg", "ケース"), actual);
    }

    @Test
    //正常系　メーカー一覧取得
    void makers_shouldReturnAllMakers() {
        Maker maker = new Maker();
        maker.setId(1);
        maker.setName("メーカーA");
        List<Maker> expected = List.of(maker);
        when(makerService.findAll()).thenReturn(expected);

        List<Maker> actual = masterApiController.makers();

        assertSame(expected, actual);
        assertEquals(maker, actual.get(0));
        verify(makerService).findAll();
    }

    @Test
    //境界値　メーカーが存在しない場合
    void makers_shouldReturnEmptyListWhenNoMakersExist() {
        List<Maker> expected = List.of();
        when(makerService.findAll()).thenReturn(expected);

        List<Maker> actual = masterApiController.makers();

        assertSame(expected, actual);
        assertEquals(0, actual.size());
        verify(makerService).findAll();
    }

    @Test
    //例外系　メーカー取得時の例外
    void makers_shouldPropagateServiceException() {
        RuntimeException expected = new RuntimeException("maker lookup failed");
        when(makerService.findAll()).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> masterApiController.makers());

        assertSame(expected, actual);
        verify(makerService).findAll();
    }
}