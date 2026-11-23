package com.joyevents.platform.repository;

import com.joyevents.platform.model.EPoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EPosterRepository extends JpaRepository<EPoster, Long> {

    List<EPoster> findByConferenceCodeOrderByPosterNumberAsc(String conferenceCode);

    List<EPoster> findByConferenceCodeAndTrackOrderByPosterNumberAsc(String conferenceCode, String track);

    Optional<EPoster> findByConferenceCodeAndPosterNumberIgnoreCase(String conferenceCode, String posterNumber);

    @Query("SELECT p FROM EPoster p WHERE p.conferenceCode = :confCode AND (" +
           "LOWER(p.posterNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.primaryAuthor) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.coAuthors) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.institution) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.track) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<EPoster> searchPosters(@Param("confCode") String confCode, @Param("q") String q);
}
