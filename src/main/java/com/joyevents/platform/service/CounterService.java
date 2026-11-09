package com.joyevents.platform.service;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.Redemption;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.RedemptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CounterService {

    private final DelegateRepository delegateRepository;
    private final CategoryRuleRepository categoryRuleRepository;
    private final RedemptionRepository redemptionRepository;

    // Dependency Injection: Spring plugs in all 3 repositories automatically
    public CounterService(DelegateRepository delegateRepository,
                          CategoryRuleRepository categoryRuleRepository,
                          RedemptionRepository redemptionRepository) {
        this.delegateRepository = delegateRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.redemptionRepository = redemptionRepository;
    }

    /**
     * Helper DTO to send clear scan results back to the mobile phone screen.
     */
    public record ScanResult(
        boolean success,
        String message,
        String delegateName,
        String registrationId,
        String category,
        int allowed,
        int issued,
        int remaining
    ) {}

    /**
     * Atomic Kit Bag Verification & Redemption.
     */
    @Transactional
    public ScanResult redeemKit(String qrToken, String conferenceCode, String counterName, String operatorUsername) {
        // 1. Find the delegate by their scanned QR token
        Optional<Delegate> delegateOpt = delegateRepository.findByQrToken(qrToken);
        if (delegateOpt.isEmpty()) {
            return new ScanResult(false, "INVALID QR CODE: Delegate not found in system.", null, null, null, 0, 0, 0);
        }

        Delegate delegate = delegateOpt.get();

        // 2. Validate Conference: Ensure QR code belongs to this specific conference
        if (!delegate.getConference().getCode().equalsIgnoreCase(conferenceCode)) {
            return new ScanResult(false, "WRONG CONFERENCE: This badge belongs to another event!", delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), 0, 0, 0);
        }

        // 3. Find the Kit allowance for this category (e.g. Faculty = 2, Exhibitor = 0)
        int allowedKits = 1; // default
        Optional<CategoryRule> ruleOpt = categoryRuleRepository.findByConferenceAndCategoryNameIgnoreCase(delegate.getConference(), delegate.getCategory());
        if (ruleOpt.isPresent()) {
            allowedKits = ruleOpt.get().getKitsAllowed();
        }

        // 4. Count how many kits this doctor has already collected
        int alreadyIssued = (int) redemptionRepository.countByDelegateAndItemType(delegate, "KIT");

        if (alreadyIssued >= allowedKits) {
            return new ScanResult(false, "REJECTED: Kit entitlement exhausted (" + alreadyIssued + "/" + allowedKits + " collected).",
                delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), allowedKits, alreadyIssued, 0);
        }

        // 5. Success: Save the redemption transaction
        Redemption redemption = new Redemption(delegate.getConference(), delegate, "KIT", 1, counterName, operatorUsername);
        redemptionRepository.save(redemption);

        int remaining = allowedKits - (alreadyIssued + 1);
        return new ScanResult(true, "KIT ISSUED SUCCESSFULLY",
            delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), allowedKits, alreadyIssued + 1, remaining);
    }

    /**
     * Atomic Lunch Verification & Anti-Passback (Double Claim Prevention).
     */
    @Transactional
    public ScanResult redeemLunch(String qrToken, String conferenceCode, int dayNumber, String counterName, String operatorUsername) {
        // 1. Find delegate by QR token
        Optional<Delegate> delegateOpt = delegateRepository.findByQrToken(qrToken);
        if (delegateOpt.isEmpty()) {
            return new ScanResult(false, "INVALID QR CODE: Delegate not found.", null, null, null, 0, 0, 0);
        }

        Delegate delegate = delegateOpt.get();

        // 2. Validate Conference
        if (!delegate.getConference().getCode().equalsIgnoreCase(conferenceCode)) {
            return new ScanResult(false, "WRONG EVENT: This badge is not registered for this conference!", delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), 0, 0, 0);
        }

        // 3. Find Lunch allowance for this category
        int allowedLunches = 1; // default per day
        Optional<CategoryRule> ruleOpt = categoryRuleRepository.findByConferenceAndCategoryNameIgnoreCase(delegate.getConference(), delegate.getCategory());
        if (ruleOpt.isPresent()) {
            allowedLunches = ruleOpt.get().getLunchesAllowed();
        }

        // 4. Count how many lunches this doctor already collected TODAY (dayNumber)
        int alreadyIssuedToday = (int) redemptionRepository.countByDelegateAndItemTypeAndDayNumber(delegate, "LUNCH", dayNumber);

        if (alreadyIssuedToday >= allowedLunches) {
            return new ScanResult(false, "ALREADY CLAIMED: Day " + dayNumber + " lunch already collected!",
                delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), allowedLunches, alreadyIssuedToday, 0);
        }

        // 5. Success: Save the lunch transaction
        Redemption redemption = new Redemption(delegate.getConference(), delegate, "LUNCH", dayNumber, counterName, operatorUsername);
        redemptionRepository.save(redemption);

        int remaining = allowedLunches - (alreadyIssuedToday + 1);
        return new ScanResult(true, "LUNCH VERIFIED: Day " + dayNumber + " lunch approved!",
            delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), allowedLunches, alreadyIssuedToday + 1, remaining);
    }
}