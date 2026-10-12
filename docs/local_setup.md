# Local Setup and Development Guide

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  

---

## 1. Prerequisites

Before running the application locally, ensure the following software tools are installed on your workstation:

| Prerequisite | Minimum Version | Verified Version on Host | Purpose |
| :--- | :--- | :--- | :--- |
| **Java Development Kit (JDK)** | JDK 21 LTS | Oracle JDK 26.0.1 / 21 LTS | Compiling and executing Spring Boot application |
| **Apache Maven** | 3.8.x or 3.9.x | Maven 3.9.16 | Dependency management and build lifecycle |
| **Web Browser** | Edge, Chrome, or Firefox | Microsoft Edge 155+ | Web dashboard visualization & Selenium testing |
| **Git** | 2.x+ | Git for Windows | Version control and workflow management |
| **Docker (Optional)** | 24.x+ | Docker Desktop / Daemon | Containerization & isolated deployment |

---

## 2. Clone and Initial Setup

Clone the repository to your local workspace:

```bash
git clone https://github.com/shaziyashaikh/Public-Parking-DevOps.git
cd Public-Parking-DevOps
```

Inspect the environment and ensure Java and Maven are present on your `PATH`:

```bash
mvn -version
java -version
```

---

## 3. Building the Application

### 3.1 Run Automated Tests
Execute the unit and service integration test suite:

```bash
mvn clean test
```
*Expected Result:* `BUILD SUCCESS` with all 33 unit and MockMvc integration tests passing.

### 3.2 Package the Runnable JAR
Package the fat Spring Boot executable archive:

```bash
mvn package
```
*Expected Result:* Output JAR located at `target/public-parking-dashboard-1.0.0.jar`.

---

## 4. Running the Application Locally

### 4.1 Launch with Maven Spring Boot Plugin
```bash
mvn spring-boot:run
```

### 4.2 Launch Directly via Java Runtime
```bash
java -jar target/public-parking-dashboard-1.0.0.jar
```

The application will start within 4 to 6 seconds:
- **Port:** `8080`
- **Application URL:** [http://localhost:8080/](http://localhost:8080/)
- **REST API Base URL:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **Actuator Health Endpoint:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:parkingdb`
  - Username: `sa`
  - Password: *(blank)*

---

## 5. Verifying Application Health

Run the automated PowerShell health verification script:

```powershell
.\scripts\health-check.ps1
```

Or execute curl / REST calls:

```bash
curl -s http://localhost:8080/actuator/health
```

Expected output:
```json
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

---

## 6. Running End-to-End Selenium Tests

Start the application first in one terminal window, then trigger the Selenium test profile in a second terminal:

```bash
mvn test "-Dtest=ParkingDashboardSeleniumTest" "-Dapp.base.url=http://localhost:8080" "-Dselenium.headless=true" "-Dselenium.browser=edge"
```

Screenshots for any failed tests will be automatically saved under `target/selenium-screenshots/`.
