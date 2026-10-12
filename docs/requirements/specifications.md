# Software Requirements Specification (SRS) & Agile Backlog

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Course:** DevOps & Cloud Computing Engineering  
**Version:** 1.0.0  

---

## 1. Problem Definition & Scope

### 1.1 Problem Statement
Urban vehicular congestion is heavily exacerbated by drivers cruising in search of available parking spaces. In major metropolitan areas, cruising accounts for up to 30% of downtown traffic congestion, leading to excessive carbon emissions, wasted fuel, driver frustration, and emergency vehicle delays. Conventional parking lots often operate as isolated silos with no real-time availability telemetry, manual ticketing errors, and lack of integration with centralized smart-city dashboards.

### 1.2 System Scope
The **DevOps-Enabled Public Parking Availability Dashboard** (ParkWatch) is an enterprise-grade, cloud-ready solution providing:
- Real-time tracking of parking lots, total capacity, occupied spots, and calculated availability.
- Dynamic categorization by spot types (Standard, EV Charging, Accessible, Reserved/VIP).
- Anomaly detection triggering automated operational alerts when lots reach high (>85%) or critical (>95%) capacity.
- Complete event audit logging capturing vehicle check-ins, check-outs, and maintenance transitions.
- A fully automated DevOps lifecycle integrating Git, automated testing (JUnit 5 + Selenium), Jenkins CI/CD pipelines, Docker containerization, and Ansible configuration management.

---

## 2. Stakeholder Analysis

| Stakeholder Role | Needs & Expectations | System Touchpoint |
| :--- | :--- | :--- |
| **City Commuters / Drivers** | Fast, mobile-responsive view of real-time spot availability and EV charging locations. | Public Web Dashboard, Search & Filter Views |
| **Parking Facility Operators** | Accurate occupancy updates, alert notifications on saturation, event logging. | Operator Admin Portal, Check-In/Check-Out APIs |
| **Municipal Traffic Authorities** | Aggregated city-wide parking telemetry, trend analytics, congestion mitigation data. | Analytics APIs, System Health and Metrics |
| **DevOps / Platform Engineers** | Zero-downtime deployments, automated test gating, reproducible container builds, configuration management. | Git, Jenkins CI/CD, Dockerfile, Ansible Playbooks |
| **Academic Evaluators** | Transparent engineering artifacts, robust test coverage, adherence to all 15 DevOps milestones. | Comprehensive Documentation, Progress Matrix, Git commit logs |

---

## 3. Objectives & Constraints

### 3.1 Objectives
1. **Real-time Telemetry:** Provide sub-second parking status queries and immediate capacity recalculations upon event submission.
2. **Operational Resilience:** Enforce business integrity constraints (occupancy cannot exceed capacity; occupancy cannot drop below zero).
3. **Automated Quality Assurance:** Implement unit tests, service integration tests, REST API MockMvc tests, and end-to-end browser automation tests.
4. **Continuous Integration & Delivery:** Automate build verification, test gating, container packaging, and rollout with automated rollback logic.
5. **Infrastructure-as-Code:** Automate target environment configuration, Docker host provisioning, and container deployment via Ansible.

### 3.2 Constraints
- **Runtime Environment:** Java 21 LTS / Spring Boot 3.3.x.
- **Embedded Database:** In-memory H2 database with MySQL compatibility mode for rapid zero-dependency demonstration and test isolation.
- **Browser Automation:** Headless Google Chrome using WebDriverManager for cross-platform headless CI execution.
- **Security & Portability:** Non-root execution in Docker containers, environment variable parameterization, no hardcoded production secrets.

---

## 4. User Stories & Acceptance Criteria

### US-01: Real-Time Availability Overview
- **As a** commuter,  
- **I want to** view a summary dashboard of all public parking locations with their available spots and occupancy percentage,  
- **So that** I can identify open parking areas before traveling.  
- **Acceptance Criteria:**
  - Dashboard shows total locations, total capacity, current occupancy, and available spots.
  - Color-coded progress badges reflect occupancy status (Low: <60%, Moderate: 60-84%, High: 85-94%, Critical: >=95%).
  - Live data refreshes without requiring page restart.

### US-02: Search & Filter Locations
- **As a** driver,  
- **I want to** filter parking locations by name, district, or minimum available spots,  
- **So that** I can quickly find parking near my intended destination.  
- **Acceptance Criteria:**
  - Case-insensitive text search by location name and address.
  - Dropdown filter by city district.
  - Filter for locations with at least `N` free spots.

### US-03: Record Parking Events (Check-In / Check-Out)
- **As a** parking lot operator,  
- **I want to** submit check-in and check-out events for a lot,  
- **So that** occupancy levels remain strictly synchronized with actual vehicles on site.  
- **Acceptance Criteria:**
  - Submitting a check-in increments lot occupancy by the specified count and updates timestamp.
  - Submitting a check-out decrements lot occupancy.
  - Attempting to check-in beyond maximum capacity is rejected with HTTP 400 and user-friendly error message.
  - Attempting to check-out below zero is rejected.

### US-04: Automated Saturation Alerts
- **As an** operator,  
- **I want to** view active alerts for lots approaching maximum capacity,  
- **So that** I can redirect incoming vehicles or deploy traffic management personnel.  
- **Acceptance Criteria:**
  - Lots at >=85% occupancy trigger `HIGH_OCCUPANCY` warnings.
  - Lots at >=95% occupancy trigger `CRITICAL_CAPACITY` alerts.
  - Alert dashboard lists active alerts with severity badges, timestamps, and quick action links.

### US-05: DevOps Automation & Health Auditing
- **As a** DevOps engineer,  
- **I want to** trigger a single automated pipeline that tests, builds, and inspects the health of the application,  
- **So that** broken builds or failing regression tests are blocked prior to deployment.  
- **Acceptance Criteria:**
  - Maven build runs unit, integration, and E2E tests.
  - Actuator `/actuator/health` endpoint reports `UP` with database and disk indicators.
  - Jenkins pipeline stops execution if any test fails.

---

## 5. Product Backlog & 15-Task Plan

| Task ID | Milestone Name | Key Deliverables | Target Status |
| :--- | :--- | :--- | :--- |
| **Task 01** | Project Initialization & Architecture Design | Requirements, architecture diagrams, Maven setup | VERIFIED |
| **Task 02** | Git Repository & Branching Strategy | Git repository, commit history, .gitignore, branching plan | VERIFIED |
| **Task 03** | Domain Modeling & Database Schema | Entities (ParkingLocation, ParkingEvent), JPA repositories | VERIFIED |
| **Task 04** | Core Service Layer & Business Validation | ParkingLocationService, business validation, alert triggers | VERIFIED |
| **Task 05** | RESTful API Implementation | ParkingApiController, DTOs, CRUD & event endpoints | VERIFIED |
| **Task 06** | Responsive Web UI (Thymeleaf + CSS) | Dashboard, detail views, event forms, alerts page | VERIFIED |
| **Task 07** | Unit & Service Integration Testing | JUnit 5 tests, Mockito, MockMvc controller tests | VERIFIED |
| **Task 08** | Browser Automation (Selenium E2E) | 7 Selenium tests, headless execution, screenshot capture | VERIFIED |
| **Task 09** | Application Health & Actuator Telemetry | Spring Boot Actuator, custom health checks, ping endpoint | VERIFIED |
| **Task 10** | Containerization (Docker) | Multi-stage Dockerfile, .dockerignore, container validation | IMPLEMENTED |
| **Task 11** | Reverse Proxy & Network Architecture | Nginx configuration, port mapping, proxy headers | IMPLEMENTED |
| **Task 12** | Continuous Integration (Jenkins Pipeline) | Declarative Jenkinsfile, quality gates, test archival | IMPLEMENTED |
| **Task 13** | Infrastructure as Code (Ansible) | Playbook, group_vars, inventory example, idempotency | IMPLEMENTED |
| **Task 14** | Automated Rollback & Disaster Recovery | PowerShell & Bash rollback scripts, failure simulation | IMPLEMENTED |
| **Task 15** | Comprehensive Documentation & Viva Preparation | PROGRESS.md, README.md, diagrams, viva Q&A | VERIFIED |

---

## 6. Definition of Done (DoD)

A task or feature is considered **Done** only when:
1. **Code Complete:** Clean, documented Java/HTML/CSS code adhering to Spring Boot best practices.
2. **Business Rule Verification:** Boundary and validation constraints tested and enforced.
3. **Automated Testing:** Unit and integration tests pass cleanly with zero failures.
4. **Build Verification:** `mvn clean test` and `mvn package` succeed without warnings.
5. **No Regressions:** Existing functionality remains intact across web and REST layers.
6. **Documentation Updated:** `docs/PROGRESS.md` updated with commands executed and actual evidence.
