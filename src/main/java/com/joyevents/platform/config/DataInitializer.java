package com.joyevents.platform.config;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Checkpoint;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.User;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.CheckpointRepository;
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
    private final CheckpointRepository checkpointRepository;

    public DataInitializer(ConferenceRepository conferenceRepository,
                           CategoryRuleRepository categoryRuleRepository,
                           DelegateRepository delegateRepository,
                           UserRepository userRepository,
                           CheckpointRepository checkpointRepository) {
        this.conferenceRepository = conferenceRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.delegateRepository = delegateRepository;
        this.userRepository = userRepository;
        this.checkpointRepository = checkpointRepository;
    }

    @Override
    public void run(String... args) {
        if (conferenceRepository.count() > 0) {
            return;
        }

        System.out.println(">>> [Joy Events] Seeding initial conference, dynamic checkpoints, rules, and admin user...");

        // 1. Initial 3-Day Conference: PHOCON 2026
        Conference phocon = new Conference(
                "PHO26",
                "PHOCON 2026",
                "PGIMER, Chandigarh",
                LocalDate.of(2026, 10, 23),
                LocalDate.of(2026, 10, 25)
        );
        phocon.setDescription("Pediatric Hematology Oncology Conference");
        conferenceRepository.save(phocon);

        // 2. Initial 2-Day Conference: 10th PHOSSCON 2026
        Conference phoss = new Conference(
                "PHOSS",
                "10th PHOSSCON 2026",
                "PGIMER, Chandigarh",
                LocalDate.of(2026, 10, 24),
                LocalDate.of(2026, 10, 25)
        );
        conferenceRepository.save(phoss);

        // 3. Dynamic Checkpoints for PHOCON (3 Days + Gala Dinner)
        checkpointRepository.save(new Checkpoint(phocon, "Day 1 Lunch", "FOOD", 1));
        checkpointRepository.save(new Checkpoint(phocon, "Day 2 Lunch", "FOOD", 2));
        checkpointRepository.save(new Checkpoint(phocon, "Day 3 Lunch", "FOOD", 3));
        checkpointRepository.save(new Checkpoint(phocon, "Gala Dinner", "FOOD", 2));
        checkpointRepository.save(new Checkpoint(phocon, "Conference Kit Bag", "KIT", 1));

        // 4. Dynamic Checkpoints for PHOSSCON (2 Days ONLY - No Day 3!)
        checkpointRepository.save(new Checkpoint(phoss, "Day 1 Lunch", "FOOD", 1));
        checkpointRepository.save(new Checkpoint(phoss, "Day 2 Lunch", "FOOD", 2));
        checkpointRepository.save(new Checkpoint(phoss, "Conference Kit Bag", "KIT", 1));

        // 5. Category Entitlement Rules
        categoryRuleRepository.save(new CategoryRule(phocon, "Delegate", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Faculty", 2, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Organizer", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "VIP", 2, 1, true));
        categoryRuleRepository.save(new CategoryRule(phocon, "Exhibitor", 0, 1, false));

        categoryRuleRepository.save(new CategoryRule(phoss, "Delegate", 1, 1, true));
        categoryRuleRepository.save(new CategoryRule(phoss, "Faculty", 2, 1, true));

        // 6. Default Manager Account
        User adminUser = new User("phocon2026@admin.com", "password123", "Joy Events Manager", "ROLE_ADMIN");
        userRepository.save(adminUser);

        // 7. Sample Doctors
        Delegate doc1 = new Delegate(phocon, "PHO26-0001", "Dr. Aditya Sharma", "aditya@example.com", "9876543210", "PGIMER, Chandigarh", "Faculty");
        Delegate doc2 = new Delegate(phocon, "PHO26-0002", "Dr. Rajesh Kheterpal", "rajesh@example.com", "9812345678", "AIIMS, New Delhi", "Delegate");
        Delegate doc3 = new Delegate(phocon, "PHO26-0003", "Dr. Sunita Verma", "sunita@example.com", "9898989898", "Fortis Hospital", "VIP");

        delegateRepository.save(doc1);
        delegateRepository.save(doc2);
        delegateRepository.save(doc3);

        System.out.println(">>> [Joy Events] Seeding completed with dynamic checkpoints!");
    }
}