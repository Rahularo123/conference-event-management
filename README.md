# 🎟️ Joy Events — Conference Registration & Operations Management Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20File%20%2F%20JPA-blue.svg)](https://www.h2database.com/)
[![PDF Engine](https://img.shields.io/badge/PDF-OpenPDF%202.0-red.svg)](https://github.com/LibrePDF/OpenPDF)
[![QR Engine](https://img.shields.io/badge/QR%20Code-ZXing%203.5-yellow.svg)](https://github.com/zxing/zxing)
[![Architecture](https://img.shields.io/badge/Architecture-4--Tier%20MVC-purple.svg)]()

> A high-throughput, enterprise on-site conference management platform built for medical summits, academic symposiums, and national conventions (modeled after **THE ONSITE.TECH** and deployed for PGIMER Chandigarh conferences like **PHOCON**).
> 
> Engineered to operate reliably in high-pressure on-site environments: instant physical badge printing, anti-passback meal/kit privilege validation, continuous multi-counter QR scanning, hall CME tracking, dynamic certificate generation, and an interactive e-poster showcase.

---

## 📌 Table of Contents
- [Key Features & Modules](#-key-features--modules)
- [System Architecture](#-system-architecture)
- [Hardware & Printing Calibration](#-hardware--printing-calibration)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Pre-Seeded Demo Data](#-pre-seeded-demo-data)
- [API & Route Reference](#-api--route-reference)
- [Production Roadmap](#-production-roadmap)

---

## 🚀 Key Features & Modules

### 1. Executive Telemetry Dashboard (`dashboard.php` Parity)
- **Live Metric KPIs:** Immediate counts of Total Registered, Printed Badges, Active Sessions, and Scanned Headcounts.
- **Multi-Category Telemetry Cards:** Dedicated real-time breakdown cards for **Faculty**, **Delegate**, and **VIP** with badge vs. certificate completion counters (`/ B:XX / CP:XX / CD:XX`).
- **One-Click Category CSV Export:** Download filtered delegate sheets directly from telemetry cards.

### 2. Onsite Registration & Attendee Desk (`attendees.php`)
- **Omni-Search & Live Filters:** Search instantly across Registration ID, Name, Category, Institute, or Mobile.
- **Batch CSV Ingestion:** Bulk-upload delegates with automatic deduplication.
- **Direct Badge Trigger:** Instant print command per attendee with live status updates.
- **WhatsApp Dispatch:** One-click WhatsApp message triggers with dynamic credentials.

### 3. Day-Wise Attendance & Photo Proof (`attendance.php`)
- **Multi-Day Tabs:** Dedicated Day 1, Day 2, and Day 3 roll calls.
- **WebRTC Camera Capture:** In-browser live webcam snapshot capture for visual identity verification.

### 4. 4×6 Badge Print Engine & Calibration Studio (`p2.php`)
- **Thermal Printer Parity:** Optimized for standard 4×6-inch roll paper used by Zebra, TSC, and Brother commercial thermal printers.
- **Live Calibration Controls:** Top-offset slider (0–100 mm) and scale factor (100–150%) to align pre-printed conference badge stationery.
- **Server-Side PDF Badge Engine:** High-resolution OpenPDF generator stamping dynamic QR codes, delegate details, and category banners with zero browser print drift.

### 5. Session Privileges & Anti-Passback Gatekeeper (`privilagesetup.php`)
- **Meal & Kit Session Engine:** Manage Lunch (Day 1/2/3), Gala Dinner, Welcome Kit, and High Tea sessions.
- **Time-Window Enforcer:** Dynamic active/pause session toggles.
- **Strict Anti-Passback Protection:** Prevents duplicate redemption of meals or kits on the same badge with sub-second rejection.
- **Category Restrictions:** Restrict sessions by tier (e.g., Faculty-only dinners or VIP lounges).

### 6. High-Speed Counter Scanner (`scanner.html` & `/scan/{eventCode}`)
- **Mobile Dedicated View:** Ultra-responsive scanning interface designed for counter staff phones and tablets.
- **Hardware-Accelerated Camera Scanner:** Real-time ZXing video stream barcode decoder with manual ID fallback.
- **Synthesized Audio Feedback:** Web Audio API sound synthesis providing instant audio confirmation (high chime for valid, low buzzer for duplicate/invalid).

### 7. Scanning Matrix Pivot Report (`scanning-report.php`)
- **Cross-Tabulation Matrix:** Real-time grid displaying every attendee against all conference privileges.
- **Live Headcount Summary:** Column totals computing aggregate kit dispatches and meal counts for catering audits.
- **Full CSV Audit Export:** One-click matrix spreadsheet download.

### 8. Auditorium & CME Hall Tracking (`roomEntry.php`)
- **Multi-Hall Monitoring:** Door entry tracking for Main Auditorium, Hall B, Workshop Suites, and Boardrooms.
- **Real-Time Capacity Indicator:** Dynamic progress bar showing live occupancy percentages against fire safety thresholds.
- **Cumulative CME Credits:** Tracks entry and exit timestamps to compute certified credit hours.

### 9. Dynamic Certificate Design Studio & Self-Service Portal (`certificate.html`)
- **Coordinate-Based Stamping:** Visual drag-and-drop or coordinate calibration for delegate name, registration ID, and CME hours.
- **Self-Service Delegate Lookup:** Attendees look up and download their personalized A4 Landscape PDF certificate by entering their registered mobile number.

### 10. Scientific E-Poster Showcase (`eposter.php`)
- **Research Abstract Gallery:** Display scientific medical posters across specialized medical tracks (e.g., *Leukemia, Bone Marrow Transplant, Molecular Diagnostics, Solid Tumors*).
- **Telemetry Counter:** Tracks individual abstract read/view statistics.
- **Integrated Full-Screen Reader:** Embedded view of research methodology, authors, and institutional affiliations.

---

## 🏗️ System Architecture

The project adheres to a textbook **4-Tier MVC Architecture**:

```
 ┌─────────────────────────────────────────────────────────────┐
 │                       Presentation Layer                    │
 │    (HTML5, Bootstrap 5 Sharp UI, WebRTC, Web Audio API)     │
 └──────────────────────────────┬──────────────────────────────┘
                                │ HTTP / REST JSON
 ┌──────────────────────────────▼──────────────────────────────┐
 │                       Controller Layer                      │
 │ (ConferenceController, DelegateController, ScannerController│
 │   BadgePdfController, RoomTrackingController, EPoster...)   │
 └──────────────────────────────┬──────────────────────────────┘
                                │ Business Logic
 ┌──────────────────────────────▼──────────────────────────────┐
 │                        Service Layer                        │
 │  (RegistrationService, PrivilegeService, CounterService,    │
 │    BadgePdfService, CertificateService, QrCodeService...)   │
 └──────────────────────────────┬──────────────────────────────┘
                                │ Spring Data JPA / Hibernate
 ┌──────────────────────────────▼──────────────────────────────┐
 │                   Data Access / Repository                  │
 │ (DelegateRepository, ConferenceRepository, SessionRepo...)  │
 └──────────────────────────────┬──────────────────────────────┘
                                │ JDBC
 ┌──────────────────────────────▼──────────────────────────────┐
 │                      Database Storage                       │
 │      Embedded H2 File Database (./data/joyevents_db)        │
 └─────────────────────────────────────────────────────────────┘
```

---

## 🖨️ Hardware & Printing Calibration

1. **Badge Dimensions:** Standard 4×6 inch (101.6 × 152.4 mm).
2. **Thermal Driver Settings:**
   - Margins: `None / 0mm`
   - Page Size: `User Defined / 4x6 in`
   - Scaling: `100% / Actual Size`
3. **In-App Offset Compensation:** If physical pre-printed stationery shifts during feed, adjust the **Top Offset** slider in `p2.php` (0 to 100 mm) to realign without altering source code.

---

## 💻 Tech Stack

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.x
- **Data Persistence:** Spring Data JPA / Hibernate
- **Database:** H2 Database (File-backed: `./data/joyevents_db.mv.db`)
- **PDF Generation:** OpenPDF (LibrePDF 2.0.3)
- **Barcode & QR:** ZXing Core & JavaSE 3.5.3
- **Frontend:** HTML5, CSS3 (Light Mode, sharp rectangular enterprise design), Bootstrap 5, WebRTC, Web Audio API
- **Build Tool:** Apache Maven Wrapper (`./mvnw`)

---

## 📂 Project Structure

```text
d:/java project
├── data/                                # Embedded H2 persistent database files
├── src/
│   ├── main/
│   │   ├── java/com/joyevents/platform/
│   │   │   ├── controller/             # 10 REST & MVC Controllers
│   │   │   ├── model/                  # JPA Entities (Delegate, Conference, Session, etc.)
│   │   │   ├── repository/             # Spring Data JPA Repositories
│   │   │   ├── service/                # Transactional Business Services
│   │   │   ├── config/                 # Security, DataInitializer & Web Config
│   │   │   └── ConferencePlatformApplication.java
│   │   └── resources/
│   │       ├── static/                 # Frontend Views (index.html, scanner.html, certificate.html...)
│   │       └── application.properties  # System configuration
│   └── test/                           # Automated integration & unit tests
├── pom.xml                             # Maven build dependencies
└── mvnw / mvnw.cmd                     # Maven Wrapper
```

---

## ⚡ Getting Started

### Prerequisites
- **JDK 21** or later installed and configured on your `PATH`.
- Git installed.

### Installation & Execution

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Rahularo123/conference-event-management.git
   cd conference-event-management
   ```

2. **Run the application:**
   - **Windows:**
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```
   - **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```

3. **Access the application:**
   - **Main Operations Console:** [http://localhost:8080](http://localhost:8080)
   - **Mobile Counter Scanner:** [http://localhost:8080/scan/PHO26](http://localhost:8080/scan/PHO26)
   - **Certificate Download Portal:** [http://localhost:8080/certificate.html](http://localhost:8080/certificate.html)
   - **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
     *(JDBC URL: `jdbc:h2:file:./data/joyevents_db`, User: `sa`, Password: `<blank>`)*

---

## 🧪 Pre-Seeded Demo Data

When launched, `DataInitializer.java` automatically initializes a production-like medical conference environment:
- **Event:** `PHO26` — *Pediatric Hematology Oncology National Conference 2026*
- **Pre-loaded Attendees:** Faculty, Delegates, and VIPs with complete institutional profiles (e.g., PGIMER, AIIMS New Delhi, Tata Memorial Hospital).
- **Active Sessions:** Day 1 Lunch, Gala Dinner, Conference Welcome Kit.
- **Scientific Abstracts:** Pre-loaded oncology research papers under the E-Poster module.

---

## 📡 API & Route Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Main operations dashboard & control center |
| `GET` | `/api/delegates/search` | Dynamic omni-search for attendee check-in |
| `POST` | `/api/delegates` | Register new delegate on-site |
| `POST` | `/api/delegates/batch-upload` | Ingest attendees from CSV file |
| `POST` | `/api/counter/scan` | High-speed QR badge scan & validation |
| `GET` | `/api/counter/matrix/{eventId}`| Cross-tabulation scanning matrix |
| `GET` | `/api/badges/pdf/{id}` | Generate pixel-calibrated 4×6 physical badge PDF |
| `GET` | `/api/certificates/download` | Generate accredited A4 landscape CME certificate PDF |
| `POST` | `/api/room-tracking/scan` | Audit hall / CME session door entrance |
| `GET` | `/api/eposters` | Fetch e-poster research abstracts by medical track |

---

## 🗺️ Production Roadmap

- [ ] **Dual Environment Profile:** Dedicated `application-prod.properties` with PostgreSQL datasource support.
- [ ] **Meta Cloud / WATI WhatsApp Gateway:** Automated delivery of digital badge QR codes upon registration.
- [ ] **Razorpay Payment Gateway:** Direct on-site card/UPI spot payment integration for spot registrations.
- [ ] **Multi-Tenant Conference Creation:** Super-admin wizard to create independent conference instances from a clean slate.

---

## 📄 License
Internal proprietary software developed for **Joy Events**. All rights reserved.
