package com.example1.demo3.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.service.MakerService;

@ExtendWith(MockitoExtension.class)
public class MakerServiceTest {
    
    @Mock
    private MakerRepository makerRepository;

    @InjectMocks
    private MakerService makerService;

    @Test
    void findAll_shouldReturnAllMaker() {
        //準備
        List<Maker> makers = List.of(new Maker(), new Maker());
        when(makerRepository.findAll()).thenReturn(makers);

        //実行
        List<Maker> result = makerService.findAll();

        //検証
        assertEquals(makers, result);
        verify(makerRepository).findAll();
    }

    @Test
    void findById_shouldReturnMaker() {
        //準備
        Maker maker = new Maker();
        when(makerRepository.findById(1)).thenReturn(Optional.of(maker));

        //実行
        Maker result = makerService.findById(1);

        //検証
        assertEquals(maker, result);
        verify(makerRepository).findById(1);
    }
    @Test
    void save_shouldCallRepositorySave() {
        //準備
        Maker maker = new Maker();

        //実行
        makerService.save(maker);

        //検証
        verify(makerRepository).save(maker);
    }
    @Test
    void delete_shouldCallRepositoryDeleteById() {

        //実行
        makerService.delete(1);

        //検証
        verify(makerRepository).deleteById(1);
    }
}
