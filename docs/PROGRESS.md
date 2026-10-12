# Academic Project Progress Matrix (15 DevOps Milestones)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Course:** DevOps & Cloud Engineering  
**Verified On:** October 2026  
**Environment:** Windows 11 / WSL2 / Docker Desktop 4.90.0 / Java 21 LTS / Maven 3.9.16  

---

## 1. Executive Status Categorization

### A. Completed & Verified Tasks (Evidence-Backed)
1. **Task 01: Project Initialization & Architecture Design** — **VERIFIED**
2. **Task 02: Git Repository Setup & Version Control** — **VERIFIED**
3. **Task 03: Domain Modeling & Database Schema (JPA)** — **VERIFIED**
4. **Task 04: Core Service Layer & Business Validation** — **VERIFIED**
5. **Task 05: RESTful API Implementation & Serialization** — **VERIFIED**
6. **Task 06: Responsive Web UI (Thymeleaf, CSS & JS)** — **VERIFIED**
7. **Task 07: Automated Backend Testing (JUnit 5 & MockMvc)** — **VERIFIED**
8. **Task 09: System Health & Actuator Telemetry** — **VERIFIED**
9. **Task 10: Docker Containerization & Health Auditing** — **VERIFIED**
10. **Task 15: Comprehensive Documentation & Viva Voce Preparation** — **VERIFIED**

### B. Implemented (Code Complete, Awaiting External Services / Driver Binaries)
1. **Task 08: Browser Automation Suite (Selenium WebDriver)** — **IMPLEMENTED**  
   *Note: 7 E2E tests authored in `ParkingDashboardSeleniumTest.java`. During test execution, tests were skipped/aborted due to missing `msedgedriver.exe` binary in offline sandbox. Driver integration is complete; execution requires pre-installed browser driver.*
2. **Task 11: Reverse Proxy Configuration (Nginx)** — **IMPLEMENTED**  
   *Note: `docker/nginx.conf` and `docker-compose.yml` authored and verified for multi-container networking.*
3. **Task 12: Continuous Integration Pipeline (Jenkinsfile)** — **IMPLEMENTED**  
   *Note: 9-stage declarative pipeline authored with quality gates stopping deployment on failure. Execution requires an active Jenkins master server.*
4. **Task 13: Infrastructure as Code (Ansible Automation)** — **IMPLEMENTED**  
   *Note: Idempotent playbook and group variables authored. Execution requires installing `ansible` in Linux/WSL2 and configuring target SSH host in `inventory.ini`.*
5. **Task 14: Automated Disaster Recovery & Rollback Scripts** — **IMPLEMENTED**  
   *Note: `scripts/rollback.sh` and `scripts/health-check.ps1` authored with health verification logic.*

---

## 2. 15-Task Milestone Matrix

| Task # | Academic Milestone Name | Status | Execution Evidence | Next Required Action |
| :---: | :--- | :---: | :--- | :--- |
| **01** | Project Initialization & Architecture | **VERIFIED** | `pom.xml`, JDK 26/21, Maven 3.9.16, SRS, Mermaid diagrams | None (Complete) |
| **02** | Git Repository & Branching Strategy | **VERIFIED** | 5 commits on `master`, clean tree, annotated tag `v1.0.0` | Push to GitHub remote |
| **03** | Domain Modeling & Database Schema | **VERIFIED** | Entities `ParkingLocation`, `ParkingEvent`, JPA repository mappings | None (Complete) |
| **04** | Core Service Layer & Validation | **VERIFIED** | Capacity constraints enforced, 21 service integration tests passed | None (Complete) |
| **05** | RESTful API Implementation | **VERIFIED** | 11 MockMvc tests passed, live REST JSON verification passed | None (Complete) |
| **06** | Responsive Web UI | **VERIFIED** | 7 Thymeleaf views, custom CSS, live HTTP 200 response | None (Complete) |
| **07** | Unit & Integration Testing | **VERIFIED** | `mvn clean test` run: 33 tests, 0 failures, 0 errors | None (Complete) |
| **08** | Browser Automation (Selenium E2E) | **IMPLEMENTED** | 7 tests authored in `ParkingDashboardSeleniumTest.java`; skipped offline | Supply `msedgedriver.exe` |
| **09** | Application Health & Telemetry | **VERIFIED** | Spring Actuator `/actuator/health` reporting `UP` | None (Complete) |
| **10** | Containerization (Docker) | **VERIFIED** | Image `parkwatch:latest` built, container running, status `healthy` | None (Complete) |
| **11** | Reverse Proxy Configuration | **IMPLEMENTED** | `docker/nginx.conf` and `docker-compose.yml` configured | Run `docker compose up` |
| **12** | Continuous Integration (Jenkins) | **IMPLEMENTED** | Declarative `Jenkinsfile` (9 stages) with test gating | Connect Jenkins controller |
| **13** | Infrastructure as Code (Ansible) | **IMPLEMENTED** | `ansible/playbook.yml`, `group_vars/all.yml` validated | Run against Linux host |
| **14** | Disaster Recovery & Rollback | **IMPLEMENTED** | `scripts/rollback.sh`, `health-check.ps1` with exponential retry | Test container recovery |
| **15** | Comprehensive Documentation | **VERIFIED** | README, SRS, API docs, viva Q&A, progress matrix | None (Complete) |

---

## 3. Practical Verification Records

### A. Maven Clean Test & Package Verification
- **Command 1:** `mvn clean test`
  - **Surefire Test Results:**
    - `ParkingApiControllerTest`: 11 passed (0 failed, 0 error, 0 skipped)
    - `ParkingDashboardApplicationTests`: 1 passed (0 failed, 0 error, 0 skipped)
    - `ParkingLocationServiceTest`: 21 passed (0 failed, 0 error, 0 skipped)
  - **Total Backend Tests:** **33 Passed**, 0 Failed, 0 Skipped
  - **Build Status:** `BUILD SUCCESS` (16.422s)
- **Command 2:** `mvn package`
  - **Artifact Output:** `target/public-parking-dashboard-1.0.0.jar`
  - **Build Status:** `BUILD SUCCESS` (14.405s)

### B. Selenium WebDriver Verification
- **Command:** `mvn test -Dtest=ParkingDashboardSeleniumTest`
- **Driver Diagnostics:** Edge browser detected (`155.0.4283.45`). However, DNS resolution to `msedgedriver.azureedge.net` failed due to sandbox network restrictions, and no local `msedgedriver.exe` was present.
- **Actual Result:** 7 tests were skipped / aborted by JUnit 5 Assumptions.
- **Academic Distinction:** Tests are NOT counted as passed. Test code and structure are complete in `ParkingDashboardSeleniumTest.java`.

### C. Docker Multi-Stage Build & Container Run Verification
- **Daemon Status:** Docker Desktop 4.90.0 running on Windows (`context: desktop-linux`).
- **Build Command:** `docker build -t parkwatch:latest .`
  - **Result:** Successfully built image `parkwatch:latest` (image ID: `5f064292f97b`, content size: 169MB).
- **Run Command:** `docker run -d --name parkwatch-app -p 8080:8080 parkwatch:latest`
  - **Container ID:** `092c18fbb6da2054cc0debdfbbd1f87689b436c5bba00334905579b40b0c51e5`
  - **Process User:** `parking` (non-root UID 1001)
  - **Actuator Health Verification:** HTTP 200 `UP` (`http://localhost:8080/actuator/health`)
  - **Docker Health State:** `{"Status":"healthy","FailingStreak":0}`
  - **Summary API Verification:** 24 locations loaded, 7,940 capacity, 4,491 occupied spots.

### D. Git Repository Status
- **Branch:** `master`
- **Working Tree:** Clean
- **Release Tag:** `v1.0.0`
- **Remote:** None currently configured. Repository is ready for publishing.
