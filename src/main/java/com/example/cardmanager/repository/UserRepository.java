package com.example.cardmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cardmanager.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
