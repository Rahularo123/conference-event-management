package com.joyevents.platform.config;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Checkpoint;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.ConferenceRoom;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.EPoster;
import com.joyevents.platform.model.PrivilegeSession;
import com.joyevents.platform.model.User;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.CheckpointRepository;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.ConferenceRoomRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.EPosterRepository;
import com.joyevents.platform.repository.PrivilegeSessionRepository;
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
    private final ConferenceRoomRepository roomRepository;
    private final PrivilegeSessionRepository privilegeSessionRepository;
    private final EPosterRepository ePosterRepository;

    public DataInitializer(ConferenceRepository conferenceRepository,
                           CategoryRuleRepository categoryRuleRepository,
                           DelegateRepository delegateRepository,
                           UserRepository userRepository,
                           CheckpointRepository checkpointRepository,
                           ConferenceRoomRepository roomRepository,
                           PrivilegeSessionRepository privilegeSessionRepository,
                           EPosterRepository ePosterRepository) {
        this.conferenceRepository = conferenceRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.delegateRepository = delegateRepository;
        this.userRepository = userRepository;
        this.checkpointRepository = checkpointRepository;
        this.roomRepository = roomRepository;
        this.privilegeSessionRepository = privilegeSessionRepository;
        this.ePosterRepository = ePosterRepository;
    }

    @Override
    public void run(String... args) {
        if (conferenceRepository.count() > 0) {
            seedRoomsAndPrivilegesIfEmpty();
            return;
        }

        System.out.println(">>> [Joy Events] Seeding initial conference, dynamic checkpoints, rules, and admin user...");

        // 1. Initial 3-Day Conference: PHOCON 2026
        Conference phocon = new Conference(
                "PHO26",
                "PHOCON 2026",
                "PGIMER, Chandigarh",
                "https://maps.app.goo.gl/aVjptnpTjBRUhV4n6",
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
                "https://maps.app.goo.gl/aVjptnpTjBRUhV4n6",
                LocalDate.of(2026, 10, 24),
                LocalDate.of(2026, 10, 25)
        );
        phoss.setDescription("Pediatric Hematology Oncology Support Services Conference");
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

        // 8. Sample Conference Halls (with Configurable CME Credits)
        seedRoomsAndPrivilegesIfEmpty();

        System.out.println(">>> [Joy Events] Seeding completed with dynamic checkpoints!");
    }

    private void seedRoomsAndPrivilegesIfEmpty() {
        if (roomRepository.count() == 0) {
            roomRepository.save(new ConferenceRoom("PHO26", "Bhargava Auditorium (Main Hall)", 350, "Keynote: Pediatric Leukemia Frontiers", 1.5));
            roomRepository.save(new ConferenceRoom("PHO26", "Lecture Hall B (Workshops)", 120, "Hands-on: Flow Cytometry Data Analysis", 2.0));
        }
        if (privilegeSessionRepository.count() == 0) {
            LocalDate day1 = LocalDate.of(2026, 10, 23);
            privilegeSessionRepository.save(new PrivilegeSession(
                    "PHO26", "Day 1 - Lunch", "FOOD", 1,
                    day1.atTime(12, 0), day1.atTime(16, 0), ""
            ));
            privilegeSessionRepository.save(new PrivilegeSession(
                    "PHO26", "Day 2 - Lunch", "FOOD", 2,
                    day1.plusDays(1).atTime(12, 0), day1.plusDays(1).atTime(16, 0), ""
            ));
            privilegeSessionRepository.save(new PrivilegeSession(
                    "PHO26", "Conference Kit Bag", "KIT", 1,
                    day1.atTime(8, 0), day1.atTime(20, 0), ""
            ));
            privilegeSessionRepository.save(new PrivilegeSession(
                    "PHO26", "Gala Banquet Dinner", "BANQUET", 2,
                    day1.plusDays(1).atTime(19, 30), day1.plusDays(1).atTime(23, 30), "Faculty,VIP"
            ));
        }
        if (ePosterRepository.count() == 0) {
            ePosterRepository.save(new EPoster(
                    "PHO26", "EP-101",
                    "Targeted Immunotherapy with Blinatumomab in Relapsed / Refractory Pediatric B-Cell ALL: Real-World Experience from North India",
                    "Dr. Ananya Ray", "Dr. Aditya Sharma, Dr. Deepak Bansal",
                    "Advanced Pediatric Centre (APC), PGIMER Chandigarh",
                    "Pediatric Leukemia",
                    "Background: Relapsed B-cell Acute Lymphoblastic Leukemia carries poor outcomes in resource-limited settings. Methods: A retrospective observational study of 24 pediatric patients treated between 2022-2025. Results: Complete molecular remission achieved in 75% of patients with manageable neurotoxicity. Conclusion: Blinatumomab serves as a safe and effective bridge to allogeneic hematopoietic stem cell transplantation.",
                    "/posters/ep101.pdf"
            ));
            ePosterRepository.save(new EPoster(
                    "PHO26", "EP-102",
                    "Evaluating Haploidentical Stem Cell Transplantation with Post-Transplant Cyclophosphamide in High-Risk Pediatric Solid Tumors",
                    "Dr. Rajesh Kheterpal", "Dr. Sunita Verma, Dr. Amita Trehan",
                    "Department of Medical Oncology, AIIMS New Delhi",
                    "Stem Cell Transplant",
                    "Background: Haploidentical donor availability expands BMT access for children lacking HLA-matched siblings. Methods: 18 consecutive pediatric patients undergoing T-replete haploidentical SCT with PTCy. Results: 1-year overall survival was 68% with low incidence of severe acute GVHD (Grade III-IV: 11%). Conclusion: PTCy-based haploidentical SCT is viable and cost-effective in developing healthcare systems.",
                    "/posters/ep102.pdf"
            ));
            ePosterRepository.save(new EPoster(
                    "PHO26", "EP-103",
                    "High-Resolution Multiparametric Flow Cytometry for Minimal Residual Disease (MRD) Detection in Pediatric AML",
                    "Dr. Priya Nair", "Dr. Manupriya Sharma, Dr. Reena Das",
                    "Department of Hematology, PGIMER Chandigarh",
                    "Diagnostics & Flow Cytometry",
                    "Background: MRD assessment at end of induction is a key prognostic determinant in Acute Myeloid Leukemia. Methods: 10-color flow cytometric MRD analysis on bone marrow aspirates of 50 pediatric AML cases. Results: Patients with MRD <0.1% demonstrated significantly higher event-free survival (78% vs 32%, p<0.001). Conclusion: Routine 10-color MFC MRD is critical for risk-stratified therapy.",
                    "/posters/ep103.pdf"
            ));
            ePosterRepository.save(new EPoster(
                    "PHO26", "EP-104",
                    "Multimodal Management of Metastatic Neuroblastoma: A Single-Center Decade Analysis of 112 Patients",
                    "Dr. Vikramaditya Sen", "Dr. Aditya Sharma, Dr. Sunita Verma",
                    "Tata Memorial Centre, Mumbai",
                    "Solid Tumors",
                    "Background: High-risk neuroblastoma remains one of the greatest challenges in pediatric oncology. Methods: Analysis of intensive induction chemotherapy, surgical resection, autologous BMT, and 131I-mIBG therapy. Results: 5-year overall survival reached 46% with dose-dense regimens. Conclusion: Aggressive multimodality approach improves outcomes in stage 4 metastatic disease.",
                    "/posters/ep104.pdf"
            ));
        }
    }
}