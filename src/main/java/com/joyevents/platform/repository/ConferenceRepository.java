package com.joyevents.platform.repository;

import com.joyevents.platform.model.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Conference.
 * Extends JpaRepository (Inheritance and Abstraction).
 */
@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {

    // Finds a conference by its code (e.g. "PHO26")
    Optional<Conference> findByCode(String code);

    // Checks if a conference code already exists
    boolean existsByCode(String code);
}