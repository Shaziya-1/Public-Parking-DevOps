# System Architecture, Use Case, Database & DevOps Diagrams

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  

---

## 1. High-Level System Architecture Diagram

```mermaid
graph TD
    subgraph ClientLayer["Client Layer"]
        Browser["Desktop & Mobile Browsers<br/>(Bootstrap 5.3 + Custom CSS + JS)"]
        RESTClient["External API Clients / IoT Sensors<br/>(HTTP / JSON)"]
    end

    subgraph IngressLayer["Edge / Ingress Layer"]
        NginxProxy["Nginx Reverse Proxy<br/>Port 80 (SSL / Rate Limiting)"]
    end

    subgraph AppLayer["Application Container (Spring Boot 3.3.4 / Java 21)"]
        Tomcat["Embedded Apache Tomcat (Port 8080)"]
        
        subgraph Controllers["Web & API Controllers"]
            WebCtrl["ParkingWebController<br/>(Thymeleaf SSR Views)"]
            ApiCtrl["ParkingApiController<br/>(REST Endpoints /api/v1/parking)"]
            ActuatorCtrl["Spring Actuator<br/>(/actuator/health, /actuator/info)"]
        end

        subgraph ServiceLayer["Business Logic & Service Layer"]
            Service["ParkingLocationService<br/>- Occupancy Math<br/>- Alert Evaluation<br/>- Event Auditing"]
            DataInit["DataInitializer<br/>(Bootstrap Seed Data)"]
        end

        subgraph DataLayer["Persistence Layer (Spring Data JPA)"]
            RepoLoc["ParkingLocationRepository"]
            RepoEvt["ParkingEventRepository"]
        end
    end

    subgraph StorageLayer["Data Storage Tier"]
        H2DB[("H2 Database (In-Memory / File)<br/>MySQL Compatibility Mode")]
    end

    Browser -->|HTTP GET/POST /| NginxProxy
    RESTClient -->|JSON REST Requests| NginxProxy
    NginxProxy -->|Proxy Pass 127.0.0.1:8080| Tomcat
    Tomcat --> WebCtrl
    Tomcat --> ApiCtrl
    Tomcat --> ActuatorCtrl
    WebCtrl --> Service
    ApiCtrl --> Service
    DataInit --> Service
    Service --> RepoLoc
    Service --> RepoEvt
    RepoLoc --> H2DB
    RepoEvt --> H2DB
```

---

## 2. Use-Case Diagram

```mermaid
graph LR
    Commuter(("Driver / Commuter"))
    Operator(("Facility Operator"))
    Admin(("DevOps / SysAdmin"))

    subgraph ParkWatchSystem["ParkWatch Parking Availability System"]
        UC1["View Real-Time Dashboard"]
        UC2["Search & Filter Locations"]
        UC3["View Detailed Occupancy & Trends"]
        UC4["Check-In Vehicle"]
        UC5["Check-Out Vehicle"]
        UC6["View Active Saturation Alerts"]
        UC7["Add / Edit Parking Location"]
        UC8["Inspect Health Metrics & Actuator"]
        UC9["Trigger Rollback / Deploy"]
    end

    Commuter --> UC1
    Commuter --> UC2
    Commuter --> UC3

    Operator --> UC1
    Operator --> UC4
    Operator --> UC5
    Operator --> UC6
    Operator --> UC7

    Admin --> UC8
    Admin --> UC9
```

---

## 3. Database Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    PARKING_LOCATIONS ||--o{ PARKING_EVENTS : "records"

    PARKING_LOCATIONS {
        BIGINT id PK "Generated Identity"
        VARCHAR(100) name "Unique parking facility name"
        VARCHAR(255) address "Physical location address"
        VARCHAR(100) city_district "District/Zone classification"
        INT total_capacity "Maximum capacity (> 0)"
        INT occupied_spaces "Current occupied count (>= 0)"
        INT ev_charging_spots "Available EV charging stations"
        INT accessible_spots "Handicapped accessible spaces"
        DECIMAL hourly_rate "Base hourly fee ($/hr)"
        VARCHAR(20) status "ACTIVE, FULL, MAINTENANCE, INACTIVE"
        TIMESTAMP created_at "Creation timestamp"
        TIMESTAMP updated_at "Last modification timestamp"
    }

    PARKING_EVENTS {
        BIGINT id PK "Generated Identity"
        BIGINT location_id FK "References PARKING_LOCATIONS(id)"
        VARCHAR(30) event_type "CHECK_IN, CHECK_OUT, MAINTENANCE_HOLD"
        INT space_delta "Change in occupied spots (+/-)"
        INT previous_occupancy "Occupancy prior to event"
        INT new_occupancy "Occupancy after event execution"
        VARCHAR(255) notes "Operator remark / vehicle telemetry tag"
        TIMESTAMP recorded_at "Event timestamp"
    }
```

---

## 4. DevOps CI/CD Lifecycle Diagram

```mermaid
graph TD
    subgraph SCM["1. Source Control Management"]
        Dev["Developer (Sajiya Shaikh)"] -->|Git Commit & Push| Feature["feature/* Branch"]
        Feature -->|Pull Request & Code Review| MainBranch["main / master Branch"]
    end

    subgraph CI["2. Continuous Integration (Jenkins Pipeline)"]
        MainBranch -->|Webhook / Poll SCM| JenkinsAgent["Jenkins Pipeline Agent"]
        JenkinsAgent --> Step1["Stage: Checkout & Environment Audit"]
        Step1 --> Step2["Stage: Compile & Code Linting"]
        Step2 --> Step3["Stage: Unit & Service Tests (JUnit 5 / MockMvc)"]
        Step3 --> Step4["Stage: Package JAR (mvn package)"]
        Step4 --> Step5["Stage: E2E Automation (Selenium Headless)"]
        Step5 --> Step6["Stage: Quality Gate Verification"]
    end

    subgraph CD["3. Continuous Delivery & Containerization"]
        Step6 -->|Pass| DockerBuild["Stage: Multi-Stage Docker Build"]
        DockerBuild --> DockerScan["Image Security Scan"]
        DockerScan --> ImageReg["Push Image to Registry (parkwatch:latest)"]
    end

    subgraph IaC["4. Deployment & Infrastructure as Code"]
        ImageReg --> AnsibleRun["Ansible Playbook Deployment"]
        AnsibleRun --> TargetHost["Target Linux / Container Host"]
        TargetHost --> HealthCheck["Automated Health Check (/actuator/health)"]
    end

    subgraph Feedback["5. Monitoring & Self-Healing"]
        HealthCheck -->|Healthy (HTTP 200)| Live["Production Live & Monitoring"]
        HealthCheck -->|Unhealthy / Degraded| AutoRollback["Automated Rollback Script (rollback.sh / .ps1)"]
        AutoRollback --> Notification["Alert DevOps Team & Restore Previous Stable Image"]
    end
```
