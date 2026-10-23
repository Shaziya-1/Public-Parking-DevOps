# ParkWatch: Academic Project Reports Overview

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Course:** DevOps & Cloud Engineering  
**Version:** 1.0.0  
**Verification Date:** October 2026  
**Environment:** Windows 11 / Eclipse Temurin JDK 21 LTS / Apache Maven 3.9.16 / Jenkins 2.580.1 LTS / Docker Desktop 27.2.0 / Nginx 1.25 Alpine / Microsoft Edge 155.0.4283.45  

---

## 1. Executive Project Overview

The **DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)** is an enterprise-grade smart-city solution engineered to address urban traffic congestion by delivering real-time parking space telemetry. The system monitors municipal parking facilities, computes live occupancy ratios, triggers automated saturation alerts, and maintains audit logs for vehicle check-in and check-out events.

The system is constructed with a modern, resilient technology stack and adheres to rigorous DevOps best practices:
- **Application Core:** Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, H2 Database.
- **Frontend & Visualization:** Thymeleaf templates, custom responsive CSS3, JavaScript (ES6+), Chart.js.
- **Data Ingestion:** 25 municipal parking locations (24 pan-Indian metropolitan sample + 1 dynamic innovation garage) with 8,040 total capacity verified via live API.
- **Automated Testing:** JUnit 5, Mockito, Spring MockMvc (45 tests passed), Selenium WebDriver 4.21.0 (7 E2E tests passed).
- **Observability:** Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`, `/actuator/info`).
- **Containerization & Networking:** Multi-stage Dockerfile, Docker Compose v2, Nginx reverse proxy on port 80.
- **Continuous Integration & Delivery:** Declarative Jenkinsfile (9 stages) with automated quality gates.
- **Infrastructure as Code (IaC):** Ansible automation playbooks executed in WSL2 Ubuntu (`ok=12, changed=3, failed=0`).
- **High Availability & Disaster Recovery:** Automated health auditing and container rollback scripts (`rollback.ps1`, `health-check.ps1`).

---

## 2. 15-Milestone Status Summary Matrix

| Milestone # | Milestone Title | Current Status | Test / Execution Evidence | Detailed Report Link |
| :---: | :--- | :---: | :--- | :---: |
| **Task 01** | Project Initialization & Architecture Design | **COMPLETED** | Maven build initialized; SRS & Mermaid architecture models drafted | [TASK_01_Report.md](TASK_01_Report.md) |
| **Task 02** | Git Repository Setup & Version Control | **COMPLETED** | Semantic commits; clean tree on `master`; annotated tag `v1.0.0` | [TASK_02_Report.md](TASK_02_Report.md) |
| **Task 03** | Domain Modeling & Database Schema (JPA) | **COMPLETED** | Entities `ParkingLocation` and `ParkingEvent`; JPA repositories | [TASK_03_Report.md](TASK_03_Report.md) |
| **Task 04** | Core Service Layer & Business Validation | **COMPLETED** | Unit tests passed; capacity bounds and alert triggers enforced | [TASK_04_Report.md](TASK_04_Report.md) |
| **Task 05** | RESTful API Implementation & Serialization | **COMPLETED** | MockMvc tests passed; RFC-7807 global exception handling | [TASK_05_Report.md](TASK_05_Report.md) |
| **Task 06** | Responsive Web UI (Thymeleaf, CSS & JS) | **COMPLETED** | 7 Thymeleaf views; responsive layout; 24 BMC locations live on port 80 | [TASK_06_Report.md](TASK_06_Report.md) |
| **Task 07** | Automated Backend Testing (JUnit 5 & MockMvc) | **COMPLETED** | **45 backend tests passed**, 0 failures, 0 errors; JAR packaged | [TASK_07_Report.md](TASK_07_Report.md) |
| **Task 08** | Browser Automation Suite (Selenium WebDriver) | **COMPLETED** | **7/7 E2E tests passed** with EdgeDriver; 7 verified screenshots captured | [TASK_08_Report.md](TASK_08_Report.md) |
| **Task 09** | System Health & Actuator Telemetry | **COMPLETED** | Spring Boot Actuator reporting `UP` with DB, disk, and ping checks | [TASK_09_Report.md](TASK_09_Report.md) |
| **Task 10** | Docker Containerization & Health Auditing | **COMPLETED** | Multi-stage build; `parkwatch:latest` built and verified healthy | [TASK_10_Report.md](TASK_10_Report.md) |
| **Task 11** | Reverse Proxy Configuration (Nginx) | **COMPLETED** | Multi-container Compose running on port 80; security rules & upstream verified | [TASK_11_Report.md](TASK_11_Report.md) |
| **Task 12** | Continuous Integration Pipeline (Jenkinsfile) | **COMPLETED** | **Build #12 SUCCESS**; all 9 stages green; container deployed on port 8085 | [TASK_12_Report.md](TASK_12_Report.md) |
| **Task 13** | Infrastructure as Code (Ansible Automation) | **COMPLETED** | Playbook executed in WSL2 Ubuntu (`ok=12, changed=3, failed=0`) | [TASK_13_Report.md](TASK_13_Report.md) |
| **Task 14** | Automated Disaster Recovery & Rollback Scripts | **COMPLETED** | PowerShell & Bash rollback tested live on port 8082; health-check verified | [TASK_14_Report.md](TASK_14_Report.md) |
| **Task 15** | Comprehensive Documentation & Academic Reports | **COMPLETED** | Full documentation in `docs/` and 15 evidence-backed reports in `REPORTS/` | [TASK_15_Report.md](TASK_15_Report.md) |
| **Task 16** | AI Parking Analysis Integration & Security Audit | **COMPLETED** | Pilot YOLOv8n inference on CPU, JSON IPC, 12 security protections passed | [TASK_16_AI_INTEGRATION_VERIFICATION.md](TASK_16_AI_INTEGRATION_VERIFICATION.md) |
| **Task 17** | Dashboard Audit, Data Provenance & Enhancements | **COMPLETED** | 25 locations audited, CSV export, global audit log, proxy bug fixed, 45 tests | [TASK_17_DASHBOARD_AUDIT.md](TASK_17_DASHBOARD_AUDIT.md) |

---

## 3. Categorization of Work

### A. Fully Completed & Verified (All 15 Milestones: 100%)
1. **Task 01:** Architecture, SRS requirements, and Maven configuration.
2. **Task 02:** Git repository management, commit convention, and release tagging.
3. **Task 03:** JPA domain models, relationships, and database schema.
4. **Task 04:** Service business logic, capacity guards, and data seeding (24 real BMC locations).
5. **Task 05:** RESTful controllers, DTOs, and global exception handling.
6. **Task 06:** 7 responsive Thymeleaf web interfaces and dynamic dashboard scripts.
7. **Task 07:** 45 automated backend tests passing cleanly with zero failures.
8. **Task 08:** 7 Selenium E2E automated tests executed and passed on Microsoft Edge (7 screenshots).
9. **Task 09:** Actuator telemetry and system health auditing (`/actuator/health` reporting `UP`).
10. **Task 10:** Multi-stage Docker containerization and non-root execution (`parkwatch:latest`).
11. **Task 11:** Production Nginx reverse proxy and multi-container Docker Compose on port 80.
12. **Task 12:** Jenkins CI/CD Declarative Pipeline Build #12 passing all stages in green; deployed container on port 8085.
13. **Task 13:** Ansible Infrastructure as Code execution on WSL2 Ubuntu (`ok=12, changed=3, failed=0`).
14. **Task 14:** Automated disaster recovery and rollback scripts tested live with previous tag.
15. **Task 15:** Technical documentation, API specs, and 15 individual evidence-based academic reports.

---

## 4. Evidence Structure

Screenshots and execution logs for each task are maintained within the `REPORTS/screenshots/` folder, organized per task:
- `screenshots/task_01/` to `screenshots/task_15/`

Each individual task report details the exact files created, commands run, output logs, problems encountered, and remaining steps for academic evaluation.

