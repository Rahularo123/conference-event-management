package com.joyevents.platform.config;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.User;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ConferenceRepository conferenceRepository;
    private final CategoryRuleRepository categoryRuleRepository;
    private final DelegateRepository delegateRepository;
    private final UserRepository userRepository;

    public DataInitializer(ConferenceRepository conferenceRepository,
                           CategoryRuleRepository categoryRuleRepository,
                           DelegateRepository delegateRepository,
                           UserRepository userRepository) {
        this.conferenceRepository = conferenceRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.delegateRepository = delegateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed data if database is brand new (empty)
        if (conferenceRepository.count() > 0) {
            return;
        }

        System.out.println(">>> [Joy Events] Seeding initial conference, rules, and admin user...");

        // 1. Initial Conference: PHOCON 2026
        Conference phocon = new Conference(
                "PHO26",
                "PHOCON 2026",
                "PGIMER, Chandigarh",
                LocalDate.of(2026, 10, 23),
                LocalDate.of(2026, 10, 25)
        );
        phocon.setDescription("Pediatric Hematology Oncology Conference");
        conferenceRepository.save(phocon);

        // 2. Second Conference: 10th PHOSSCON 2026 (Proves multi-conference capability)
        Conference phoss = new Conference(
                "PHOSS",
                "10th PHOSSCON 2026",
                "PGIMER, Chandigarh",
                LocalDate.of(2026, 10, 24),
                LocalDate.of(2026, 10, 25)
        );
        conferenceRepository.save(phoss);

        // 3. Category Entitlement Rules (Kits, Lunches, Certificates)
        categoryRuleRepository.save(new CategoryRule(phocon, "Delegate", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Faculty", 2, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Organizer", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "VIP", 2, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Exhibitor", 0, 1, false));

        categoryRuleRepository.save(new CategoryRule(phoss, "Delegate", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phoss, "Faculty", 2, 1, true));

        // 4. Default Manager Account (Section 15: Roles)
        User adminUser = new User("phocon2026@admin.com", "password123", "Joy Events Manager", "ROLE_ADMIN");
        userRepository.save(adminUser);

        // 5. Sample Doctors for immediate testing
        Delegate doc1 = new Delegate(phocon, "PHO26-0001", "Dr. Aditya Sharma", "aditya@example.com", "9876543210", "PGIMER, Chandigarh", "Faculty");
        Delegate doc2 = new Delegate(phocon, "PHO26-0002", "Dr. Rajesh Kheterpal", "rajesh@example.com", "9812345678", "AIIMS, New Delhi", "Delegate");
        Delegate doc3 = new Delegate(phocon, "PHO26-0003", "Dr. Sunita Verma", "sunita@example.com", "9898989898", "Fortis Hospital", "VIP");

        delegateRepository.save(doc1);
        delegateRepository.save(doc2);
        delegateRepository.save(doc3);

        System.out.println(">>> [Joy Events] Seeding completed! Default manager: phocon2026@admin.com");
    }
}