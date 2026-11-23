package com.joyevents.platform.repository;

import com.joyevents.platform.model.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {

    // Fetch the active certificate coordinate template for a conference
    Optional<CertificateTemplate> findByConferenceCode(String conferenceCode);
}