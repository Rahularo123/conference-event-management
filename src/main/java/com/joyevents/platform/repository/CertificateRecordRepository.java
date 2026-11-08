package com.joyevents.platform.repository;

import com.joyevents.platform.model.CertificateRecord;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRecordRepository extends JpaRepository<CertificateRecord, Long> {

    // Find all certificates belonging to a specific doctor in a conference
    List<CertificateRecord> findByConferenceAndDelegate(Conference conference, Delegate delegate);

    // Look up certificate by its unique verification code (e.g. "CERT-PHO-001")
    Optional<CertificateRecord> findByCertificateCode(String certificateCode);

    // Live counts for the dashboard
    long countByConferenceAndPrintedOnsite(Conference conference, boolean printedOnsite);
    long countByConferenceAndDownloadedOnline(Conference conference, boolean downloadedOnline);
}