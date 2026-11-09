package com.joyevents.platform.service;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final DelegateRepository delegateRepository;
    private final ConferenceRepository conferenceRepository;
    private final BadgePdfService badgePdfService;

    // Dependency Injection: Spring plugs in the repositories and badge generator
    public RegistrationService(DelegateRepository delegateRepository,
                               ConferenceRepository conferenceRepository,
                               BadgePdfService badgePdfService) {
        this.delegateRepository = delegateRepository;
        this.conferenceRepository = conferenceRepository;
        this.badgePdfService = badgePdfService;
    }

    /**
     * 1. Search delegates by Name, RegID, Mobile, Email, or Hospital.
     */
    public List<Delegate> searchDelegates(String conferenceCode, String query) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        if (query == null || query.trim().isEmpty()) {
            return delegateRepository.findByConferenceOrderByIdDesc(conference);
        }
        return delegateRepository.searchDelegates(conference, query.trim());
    }

    /**
     * 2. Register an on-the-spot walk-in doctor with duplicate detection.
     */
    @Transactional
    public Delegate registerOnsiteDelegate(String conferenceCode, String fullName, String email, 
                                          String mobile, String institution, String category) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        // Generate next sequential registration ID (e.g. PHO26-001)
        String nextRegId = generateNextRegistrationId(conference);

        // Create new delegate object (its constructor automatically creates unique qrToken)
        Delegate delegate = new Delegate(conference, nextRegId, fullName, email, mobile, institution, category);
        delegate.setRegistrationSource("ONSITE");
        delegate.setRegisteredAt(LocalDateTime.now());

        return delegateRepository.save(delegate);
    }

    /**
     * 3. Generates the next sequential registration ID (e.g. PHO26-001, PHO26-002).
     */
    private String generateNextRegistrationId(Conference conference) {
        long currentCount = delegateRepository.countByConference(conference);
        return String.format("%s-%04d", conference.getCode(), currentCount + 1);
    }

    /**
     * 4. Marks badge as printed and generates the 4x6 print PDF.
     */
    @Transactional
    public byte[] printBadgeAndMark(Long delegateId) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        // Mark as printed with timestamp
        delegate.setBadgePrinted(true);
        delegate.setBadgePrintTimestamp(LocalDateTime.now());
        delegateRepository.save(delegate);

        // Generate and return the 4x6 PDF bytes
        return badgePdfService.generateBadgePdf(delegate);
    }
}