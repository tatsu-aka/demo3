package com.example1.demo3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example1.demo3.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String Username);
}
