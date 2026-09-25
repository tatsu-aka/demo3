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

import com.example1.demo3.controller.api.MakerApiController;
import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;

@ExtendWith(MockitoExtension.class)
class MakerApiControllerTest {

    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private MakerApiController makerApiController;

    @Test
    void getMakers_shouldReturnAllMakers() {
        Maker maker = new Maker();
        maker.setId(1);
        maker.setName("メーカーA");
        List<Maker> expected = List.of(maker);
        when(makerRepository.findAll()).thenReturn(expected);

        List<Maker> actual = makerApiController.getMakers();

        assertSame(expected, actual);
        assertEquals(maker, actual.get(0));
        verify(makerRepository).findAll();
    }

    @Test
    void getMakers_shouldReturnEmptyListWhenNoMakersExist() {
        List<Maker> expected = List.of();
        when(makerRepository.findAll()).thenReturn(expected);

        List<Maker> actual = makerApiController.getMakers();

        assertSame(expected, actual);
        assertEquals(0, actual.size());
        verify(makerRepository).findAll();
    }

    @Test
    void getMakers_shouldPropagateRepositoryException() {
        RuntimeException expected = new RuntimeException("maker lookup failed");
        when(makerRepository.findAll()).thenThrow(expected);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> makerApiController.getMakers());

        assertSame(expected, actual);
        verify(makerRepository).findAll();
    }
}