package com.joyevents.platform.service;

import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.PrivilegeSession;
import com.joyevents.platform.model.Redemption;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.PrivilegeSessionRepository;
import com.joyevents.platform.repository.RedemptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PrivilegeService {

    private final PrivilegeSessionRepository sessionRepository;
    private final DelegateRepository delegateRepository;
    private final RedemptionRepository redemptionRepository;

    public PrivilegeService(PrivilegeSessionRepository sessionRepository,
                            DelegateRepository delegateRepository,
                            RedemptionRepository redemptionRepository) {
        this.sessionRepository = sessionRepository;
        this.delegateRepository = delegateRepository;
        this.redemptionRepository = redemptionRepository;
    }

    // ── 1. SESSION MANAGEMENT (privilagesetup.php) ──

    public List<PrivilegeSession> getSessions(String confCode) {
        return sessionRepository.findByConferenceCodeOrderByDayNumberAscStartTimeAsc(confCode);
    }

    public List<PrivilegeSession> getActiveSessionsNow(String confCode) {
        return sessionRepository.findActiveSessionsNow(confCode, LocalDateTime.now());
    }

    @Transactional
    public PrivilegeSession saveSession(PrivilegeSession session) {
        return sessionRepository.save(session);
    }

    @Transactional
    public PrivilegeSession toggleActive(Long sessionId) {
        PrivilegeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        session.setActive(!session.isActive());
        return sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    // ── 2. DYNAMIC PRIVILEGE REDEMPTION LOGIC ──

    public record RedemptionResult(
            boolean success,
            String message,
            String delegateName,
            String registrationId,
            String category,
            String sessionName,
            String timestamp
    ) {}

    @Transactional
    public RedemptionResult redeemPrivilege(String confCode, Long sessionId, String qrOrRegId, 
                                            String counterName, String operatorUsername) {
        // 1. Verify Privilege Session
        PrivilegeSession session = sessionRepository.findById(sessionId)
                .orElse(null);
        if (session == null || !session.isActive()) {
            return new RedemptionResult(false, "SESSION CLOSED: Counter is currently inactive.", null, null, null, null, now());
        }

        // 2. Validate Time Window
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isBefore(session.getStartTime()) || currentTime.isAfter(session.getEndTime())) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            String window = session.getStartTime().format(dtf) + " - " + session.getEndTime().format(dtf);
            return new RedemptionResult(false, "OUTSIDE TIME WINDOW: Valid only between " + window, null, null, null, session.getName(), now());
        }

        // 3. Find Delegate by QR Token or Registration ID (manual desk fallback)
        Optional<Delegate> delegateOpt = delegateRepository.findByQrToken(qrOrRegId);
        if (delegateOpt.isEmpty()) {
            delegateOpt = delegateRepository.findByRegistrationIdIgnoreCase(qrOrRegId);
        }
        if (delegateOpt.isEmpty()) {
            return new RedemptionResult(false, "INVALID BADGE: No delegate found with this QR or ID.", null, null, null, session.getName(), now());
        }

        Delegate delegate = delegateOpt.get();

        // 4. Verify Conference Match
        if (!delegate.getConference().getCode().equalsIgnoreCase(confCode)) {
            return new RedemptionResult(false, "WRONG EVENT: This badge is registered for another conference.", delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), session.getName(), now());
        }

        // 5. Category Entitlement Check (e.g. Faculty, Delegate, VIP)
        if (session.getAllowedCategories() != null && !session.getAllowedCategories().isBlank()) {
            List<String> allowed = Arrays.stream(session.getAllowedCategories().split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();
            if (!allowed.contains(delegate.getCategory().toUpperCase())) {
                return new RedemptionResult(false, "CATEGORY NOT ELIGIBLE: '" + delegate.getCategory() + "' is not eligible for " + session.getName(), 
                        delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), session.getName(), now());
            }
        }

        // 6. Anti-Passback Check (Double Claim Prevention)
        long alreadyClaimed = redemptionRepository.countByDelegateAndItemTypeAndDayNumber(
                delegate, session.getName(), session.getDayNumber() != null ? session.getDayNumber() : 1
        );

        if (alreadyClaimed > 0) {
            return new RedemptionResult(false, "ALREADY CLAIMED: This attendee has already redeemed " + session.getName() + "!", 
                    delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), session.getName(), now());
        }

        // 7. Approve & Record Redemption Transaction
        Redemption redemption = new Redemption(
                delegate.getConference(),
                delegate,
                session.getName(),
                session.getDayNumber() != null ? session.getDayNumber() : 1,
                counterName != null ? counterName : "General Counter",
                operatorUsername != null ? operatorUsername : "desk_operator"
        );
        redemptionRepository.save(redemption);

        return new RedemptionResult(true, "APPROVED: " + session.getName() + " verified!", 
                delegate.getFullName(), delegate.getRegistrationId(), delegate.getCategory(), session.getName(), now());
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }
}