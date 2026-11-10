package com.joyevents.platform.service;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.RedemptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final DelegateRepository delegateRepository;
    private final ConferenceRepository conferenceRepository;
    private final RedemptionRepository redemptionRepository;
    private final BadgePdfService badgePdfService;

    // Dependency Injection
    public RegistrationService(DelegateRepository delegateRepository,
                               ConferenceRepository conferenceRepository,
                               RedemptionRepository redemptionRepository,
                               BadgePdfService badgePdfService) {
        this.delegateRepository = delegateRepository;
        this.conferenceRepository = conferenceRepository;
        this.redemptionRepository = redemptionRepository;
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
     * 2. Register an on-the-spot walk-in doctor.
     */
    @Transactional
    public Delegate registerOnsiteDelegate(String conferenceCode, String fullName, String email, 
                                          String mobile, String institution, String category) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        String nextRegId = generateNextRegistrationId(conference);

        Delegate delegate = new Delegate(conference, nextRegId, fullName, email, mobile, institution, category);
        delegate.setRegistrationSource("ONSITE");
        delegate.setRegisteredAt(LocalDateTime.now());

        return delegateRepository.save(delegate);
    }

    /**
     * 3. Update an existing doctor's details (Fix wrong phone numbers, typos, etc.)
     */
    @Transactional
    public Delegate updateDelegate(Long delegateId, String fullName, String email, 
                                   String mobile, String institution, String category) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        delegate.setFullName(fullName);
        delegate.setEmail(email);
        delegate.setMobile(mobile);
        delegate.setInstitution(institution);
        delegate.setCategory(category);

        return delegateRepository.save(delegate);
    }

    /**
     * 4. Permanently delete a delegate record and their redemption history.
     */
    @Transactional
    public void deleteDelegate(Long delegateId) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        delegateRepository.delete(delegate);
    }

    /**
     * 5. Generates the next sequential registration ID (e.g. PHO26-0001, PHO26-0002).
     */
    private String generateNextRegistrationId(Conference conference) {
        long currentCount = delegateRepository.countByConference(conference);
        return String.format("%s-%04d", conference.getCode(), currentCount + 1);
    }

    /**
     * 6. Marks badge as printed and generates the 4x6 print PDF.
     */
    @Transactional
    public byte[] printBadgeAndMark(Long delegateId) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        delegate.setBadgePrinted(true);
        delegate.setBadgePrintTimestamp(LocalDateTime.now());
        delegateRepository.save(delegate);

        return badgePdfService.generateBadgePdf(delegate);
    }
}