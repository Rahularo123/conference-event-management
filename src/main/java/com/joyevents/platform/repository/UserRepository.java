package com.joyevents.platform.repository;

import com.joyevents.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user by their login username / email
    Optional<User> findByUsername(String username);

    // Checks if a username already exists before creating a new account
    boolean existsByUsername(String username);
}