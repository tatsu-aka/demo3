package com.example1.demo3.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example1.demo3.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    //正常系　usernameが存在する場合、ユーザーを返すことを確認する
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setRole("USER");
        userRepository.saveAndFlush(user);

        Optional<User> result = userRepository.findByUsername("alice");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
        assertEquals("USER", result.get().getRole());
    }

    @Test
    //正常系　usernameが存在しない場合、空のOptionalを返すことを確認する
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        Optional<User> result = userRepository.findByUsername("not-found-user");

        assertTrue(result.isEmpty());
    }

    @Test
    //境界値　usernameが空文字または空白文字の場合、空のOptionalを返すことを確認する
    void findByUsername_shouldReturnEmpty_whenUsernameIsBlank() {
        assertTrue(userRepository.findByUsername("").isEmpty());
        assertTrue(userRepository.findByUsername(" ").isEmpty());
    }

    @Test
    //例外系　usernameが重複している場合、DataIntegrityViolationExceptionがスローされることを確認する
    void save_shouldThrowException_whenUsernameIsDuplicated() {
        User first = new User();
        first.setUsername("bob");
        first.setPassword("pass1");
        first.setRole("USER");
        userRepository.saveAndFlush(first);

        User second = new User();
        second.setUsername("bob");
        second.setPassword("pass2");
        second.setRole("ADMIN");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(second);
        });
    }
}
