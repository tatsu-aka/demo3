package com.example1.demo3.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example1.demo3.controller.MakerController;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;

@ExtendWith(MockitoExtension.class)
class MakerControllerTest {

    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private MakerController makerController;

    @Test
    void list_shouldReturnMakerListAndInitializeForm() {
        Maker maker = new Maker();
        maker.setId(1);
        maker.setName("青果メーカー");
        List<Maker> makers = List.of(maker);
        Model model = new ExtendedModelMap();
        when(makerRepository.findAll()).thenReturn(makers);

        String viewName = makerController.list(model);

        assertEquals("maker-list", viewName);
        assertEquals(makers, model.getAttribute("makers"));
        assertEquals(Maker.class, model.getAttribute("maker").getClass());
        verify(makerRepository).findAll();
    }

    @Test
    void list_shouldReturnEmptyListAtBoundary() {
        Model model = new ExtendedModelMap();
        when(makerRepository.findAll()).thenReturn(Collections.emptyList());

        String viewName = makerController.list(model);

        assertEquals("maker-list", viewName);
        assertEquals(Collections.emptyList(), model.getAttribute("makers"));
        verify(makerRepository).findAll();
    }

    @Test
    void add_shouldSaveMakerWithMaximumNameLength() {
        Maker maker = new Maker();
        maker.setName("あ".repeat(50));

        String viewName = makerController.add(maker);

        assertEquals("redirect:/makers", viewName);
        verify(makerRepository).save(maker);
    }

    @Test
    void add_shouldPropagateRepositoryExceptionForNullName() {
        Maker maker = new Maker();
        maker.setName(null);
        when(makerRepository.save(any(Maker.class)))
            .thenThrow(new IllegalArgumentException("maker name must not be null"));

        assertThrows(IllegalArgumentException.class, () -> makerController.add(maker));
        verify(makerRepository).save(maker);
    }

    @Test
    void delete_shouldDeleteMakerWithMinimumIntegerId() {
        String viewName = makerController.delete(Integer.MIN_VALUE);

        assertEquals("redirect:/makers", viewName);
        verify(makerRepository).deleteById(Integer.MIN_VALUE);
    }
}
