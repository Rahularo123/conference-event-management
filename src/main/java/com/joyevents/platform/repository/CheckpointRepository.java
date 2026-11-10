package com.joyevents.platform.repository;

import com.joyevents.platform.model.Checkpoint;
import com.joyevents.platform.model.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Long> {

    // Returns all active checkpoints (meals, kits, workshops) for a conference
    List<Checkpoint> findByConferenceAndActiveTrue(Conference conference);

    // Returns all checkpoints (including inactive/archived)
    List<Checkpoint> findByConference(Conference conference);
}