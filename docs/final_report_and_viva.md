# Final Academic Report & Comprehensive Viva Voce Preparation

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Course:** DevOps & Cloud Engineering  
**Academic Year:** 2026  

---

## 1. Executive Summary

Urban mobility systems face critical bottlenecks due to inefficient parking spot discovery. The **DevOps-Enabled Public Parking Availability Dashboard** (ParkWatch) provides an end-to-end full-stack smart-city parking management system coupled with modern DevOps automation.

### Key Milestones Achieved:
1. **Full-Stack Application Development:** Implemented in Java 21 LTS using Spring Boot 3.3.4, Spring Data JPA, Thymeleaf SSR, Bootstrap 5.3, and custom CSS/JS.
2. **Robust Business Domain:** Supports real-time capacity management, occupancy boundary protection (occupancy cannot exceed capacity, cannot fall below zero), dynamic saturation alerts, and vehicle audit logging.
3. **Comprehensive Automated Testing:** 33 unit and service integration tests (JUnit 5, Mockito, MockMvc) with 100% pass rate, plus 7 Selenium WebDriver browser automation scenarios.
4. **CI/CD Pipeline as Code:** Declarative 9-stage `Jenkinsfile` featuring test quality gates, automated artifact archiving, Docker image creation, and automated rollbacks.
5. **Containerization & IaC:** Multi-stage `Dockerfile` with non-root security privileges and idempotent Ansible provisioning playbooks.

---

## 2. Academic Viva Voce Questions & Answers

### Section A: Spring Boot & Application Architecture

#### Q1: What is Spring Boot and why was it chosen for this project?
**Answer:**  
Spring Boot is an enterprise Java framework that minimizes boilerplate configuration through opinionated "starter" dependencies and auto-configuration. It was chosen for ParkWatch because:
1. It features an embedded Apache Tomcat web server, allowing the app to run as an independent executable JAR without needing external server setups.
2. Spring Data JPA drastically reduces data access code through automated repository interfaces.
3. Spring Boot Actuator provides production-grade telemetry endpoints (`/actuator/health`, `/actuator/info`) critical for DevOps health checking and automated deployment gates.

#### Q2: How does the application enforce business integrity (e.g. occupancy cannot exceed capacity)?
**Answer:**  
Business rules are enforced at multiple levels:
1. **Bean Validation Annotations:** `@Min(0)`, `@Max(10000)`, `@NotNull` on entity models and DTOs.
2. **Service Layer Invariants:** In `ParkingLocationService`, when an event or occupancy update is submitted, the service checks whether `newOccupancy > totalCapacity` or `newOccupancy < 0`. If violated, it throws `InvalidParkingUpdateException`.
3. **Global Exception Handling:** `@ControllerAdvice` (`GlobalExceptionHandler`) intercepts `InvalidParkingUpdateException` and transforms it into an HTTP 400 Bad Request with a clear JSON error payload for REST calls, or an alert banner for web form submissions.

#### Q3: Why is H2 Database used, and how does it transition to production?
**Answer:**  
H2 is an ultra-fast, zero-dependency in-memory relational database configured in MySQL compatibility mode (`MODE=MySQL`). It is ideal for academic demonstrations, rapid iteration, and integration testing because every test run starts with a clean, known state. In production, transitioning to MySQL or PostgreSQL requires only updating `spring.datasource.url`, driver, and credentials in `application-prod.properties` or environment variables without changing any application code.

---

### Section B: Automated Testing & Selenium

#### Q4: What is the difference between Unit Tests, MockMvc Tests, and Selenium E2E Tests?
**Answer:**  
- **Unit Tests:** Test individual classes and methods in isolation using Mockito mocks (e.g., testing calculation algorithms in `ParkingLocationServiceTest`).
- **MockMvc Tests:** Test REST controllers within a mock HTTP servlet environment without starting a real web server. They verify HTTP status codes, JSON serialization, and routing.
- **Selenium E2E Tests:** Test the fully deployed application through an actual web browser (Edge/Chrome/Firefox), validating DOM elements, CSS styles, user workflows, JavaScript interactions, and form submissions from the real user's perspective.

#### Q5: What is Headless Browser Testing and why is it essential in CI/CD?
**Answer:**  
Headless browser testing runs browser automation without rendering a visible graphical user interface (GUI). It is essential in CI/CD environments (such as Jenkins agents or Docker containers) because CI build agents are typically headless Linux servers with no attached monitor or display server (X11/Wayland). Headless mode also executes significantly faster and consumes fewer memory and CPU resources.

---

### Section C: DevOps, Jenkins CI/CD & Quality Gates

#### Q6: What is a Quality Gate in a CI/CD pipeline?
**Answer:**  
A Quality Gate is a conditional checkpoint in a CI/CD pipeline that enforces software quality standards before allowing the build to proceed to subsequent stages. In our `Jenkinsfile`, Quality Gates ensure that:
1. The code must compile with zero errors.
2. 100% of unit and integration tests must pass.
3. If any test fails, the pipeline immediately halts and marks the build as `FAILED`, preventing broken or buggy code from reaching the Docker build or deployment stages.

#### Q7: How does automated rollback work in ParkWatch?
**Answer:**  
Our deployment pipeline implements post-deployment health verification. After starting the new container version, the pipeline invokes `scripts/health-check.ps1` (or `health-check.sh`), polling `/actuator/health` up to 10 times. If the endpoint fails to return HTTP 200 `UP`, the pipeline immediately triggers `scripts/rollback.sh`, which stops the unhealthy container and rolls back to the previously tagged stable Docker image.

---

### Section D: Docker Containerization & Security

#### Q8: What are the benefits of a Multi-Stage Dockerfile?
**Answer:**  
A multi-stage Docker build separates the build environment from the runtime environment:
1. **Minimal Image Footprint:** The final image contains only the JRE and the JAR file (approx. 200MB), excluding Maven, compilers, source code, and intermediate build tools (which would inflate the image to over 800MB).
2. **Security:** Excluding build tools and compilers prevents attackers from using internal development utilities if the container is ever compromised.
3. **Layer Caching:** Dependencies downloaded in `dependency:go-offline` are cached between builds, speeding up subsequent container creation.

#### Q9: Why should containers not run as the `root` user?
**Answer:**  
Running containers as root violates the principle of least privilege. If a vulnerability exists in the application or runtime that permits remote code execution, an attacker gaining shell access inside the container would possess root privileges, potentially enabling container escape to access host files and processes. In ParkWatch, we explicitly create and switch to user `parkingapp` (UID 1001).

---

### Section E: Ansible & Infrastructure as Code (IaC)

#### Q10: What is Idempotency in Ansible and why does it matter?
**Answer:**  
Idempotency is the property where executing an automation script multiple times produces the exact same end state without causing unexpected side effects. In Ansible, tasks check the current state of the target system before applying changes:
- If Docker is already installed, Ansible skips the installation task.
- If the target directory already exists with the correct permissions, no changes are made.
This ensures stability, predictability, and prevents accidental downtime during repeated deployments.
