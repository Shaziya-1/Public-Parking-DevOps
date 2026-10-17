# Task 12 Report: Continuous Integration Pipeline (Jenkinsfile)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 12  
**Status:** **COMPLETED**  

---

## 1. Objective
Design, configure, and execute an automated 9-stage Continuous Integration and Continuous Delivery (CI/CD) pipeline using a Declarative `Jenkinsfile` on a running Jenkins controller. The pipeline automates repository checkout, runtime environment verification, compilation, automated unit/integration testing with JUnit XML report archiving, JAR packaging, Docker container image building, and containerized deployment with automated health auditing.

---

## 2. Tools and Technologies Used
- **CI/CD Server:** Jenkins 2.580.1 LTS (Windows Service running on port 8080)
- **Pipeline DSL:** Declarative Pipeline Groovy syntax (`Jenkinsfile`)
- **Version Control:** Git 2.52.0 Windows (`git.exe`, branch `master`)
- **Build Engine:** Apache Maven 3.9.16 (via Maven Wrapper `mvnw.cmd` / `mvnw`)
- **JDK:** Eclipse Adoptium Temurin JDK 21.0.12 LTS
- **Container Runtime:** Docker Desktop 27.2.0 (engine 27.2.0, API 1.47)
- **Test Reporting:** Jenkins JUnit Plugin, Surefire XML reports

---

## 3. Work Performed
1. Audited the Jenkins job `Public-Parking-DevOps` configuration and identified historical build failures:
   - **Build #9:** Blocked at Stage 1 (Checkout) by Jenkins Git Plugin security policy (SECURITY-2824) restricting local `file:///` URLs.
   - **Build #10:** SCM checkout succeeded after enabling `ALLOW_LOCAL_CHECKOUT`, but pipeline failed at Stage 1 commit-hash step because Windows Jenkins agent does not have `sh` on PATH (`CreateProcess error=2`).
   - **Build #11:** Failed in Stage 7 (`docker build`) because Windows CRLF line endings in `mvnw` caused Alpine Linux builder container to report `/bin/sh: ./mvnw: not found` (exit code 127).
2. Resolved runtime security and execution configuration:
   - Activated in-memory local checkout override via Jenkins Script Console (`http://localhost:8080/script`):
     ```groovy
     System.setProperty("hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT", "true")
     hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT = true
     ```
   - Added persistent initialization script at `C:\ProgramData\Jenkins\.jenkins\init.groovy.d\allow-local-checkout.groovy`.
3. Upgraded `Jenkinsfile` to full cross-platform compatibility:
   - Added `isUnix() ? sh(...) : bat(...)` branching across all stages to allow seamless execution on Windows controllers and Linux agents alike.
   - Added Docker Desktop binary path (`C:\Users\Admin\AppData\Local\Programs\DockerDesktop\resources\bin`) and Git paths to pipeline `environment { PATH = ... }`.
   - Changed default pipeline deployment port `APP_PORT` to `8085` to avoid port collision with host Jenkins service on `8080` and Docker Compose stack on `8081`.
4. Fixed `Dockerfile` multi-stage build:
   - Added `RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw` to normalize line endings on Windows checkouts before invoking Maven commands in Alpine Linux.
5. Executed **Build #12** live via Jenkins Web UI with parameters (`APP_PORT=8085`, `PUSH_IMAGE=false`, `RUN_SELENIUM=false`):
   - All stages completed with **SUCCESS**.
   - Generated Docker image `public-parking-dashboard:1.0.0-12` and tag `latest`.
   - Deployed container `public-parking-dashboard-container` listening on port `8085`.
6. Verified deployed application telemetry and health via `curl.exe`:
   - Endpoint `/actuator/health` responded with `{"status":"UP"}`.
   - Summary endpoint `/api/v1/dashboard/summary` responded with 24 municipal locations and 16,846 total capacity.
7. Captured official Pipeline Stage View screenshot and saved to `REPORTS/screenshots/task_12/jenkins_pipeline_success.png`.

---

## 4. Files Created or Modified
- `Jenkinsfile`: Cross-platform declarative pipeline with Windows `bat` and Linux `sh` branches, Docker PATH configuration, and `APP_PORT=8085`.
- `Dockerfile`: Multi-stage build with CRLF normalization for `mvnw`.
- `C:\ProgramData\Jenkins\.jenkins\init.groovy.d\allow-local-checkout.groovy`: Controller startup script allowing local Git checkout.
- `REPORTS/screenshots/task_12/jenkins_pipeline_success.png`: Live Jenkins Web UI screenshot showing Build #12 Stage View with all green stages.

---

## 5. Commands Executed
```powershell
# 1. Verify build status and tail build 12 console log
Get-Content 'C:\ProgramData\Jenkins\.jenkins\jobs\Public-Parking-DevOps\builds\12\log' -Tail 40

# 2. Audit deployed container health on port 8085
curl.exe -i http://localhost:8085/actuator/health

# 3. Query live API data from pipeline-deployed container
curl.exe -s http://localhost:8085/api/v1/dashboard/summary
```

### Execution Log / Terminal Output
```text
[Pipeline] Stage (Deploy)
[2026-10-17T16:35:22.996Z] Container started: public-parking-dashboard-container on port 8085
[Pipeline] Stage (Declarative: Post Actions)
[2026-10-17T16:35:24.570Z] [WS-CLEANUP] done
[2026-10-17T16:35:24.840Z] 
[2026-10-17T16:35:24.840Z]             ╔════════════════════════════════════════════╗
[2026-10-17T16:35:24.840Z]             ║   BUILD AND DEPLOYMENT SUCCESSFUL          ║
[2026-10-17T16:35:24.840Z]             ║   App: public-parking-dashboard:1.0.0-12   ║
[2026-10-17T16:35:24.840Z]             ║   Port: 8085                               ║
[2026-10-17T16:35:24.840Z]             ║   URL:  http://localhost:8085              ║
[2026-10-17T16:35:24.840Z]             ╚════════════════════════════════════════════╝
[Pipeline] End of Pipeline
Finished: SUCCESS

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> curl.exe -i http://localhost:8085/actuator/health
HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v3+json
{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}},"diskSpace":{"status":"UP","details":{"total":1081101176832,"free":1021487943680,"threshold":10485760,"path":"/app/.","exists":true}},"ping":{"status":"UP"}}}

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> curl.exe -s http://localhost:8085/api/v1/dashboard/summary
{"totalLocations":24,"totalCapacity":16846,"totalOccupied":7225,"totalAvailable":9621,"overallUtilizationPercentage":42.9,"fullLocations":3,"lowAvailabilityLocations":5,"staleLocations":4,"lowAvailabilityThreshold":85}
```

---

## 6. Actual Test Results & Verification Evidence
- **Build Number:** **#12**
- **Triggered By:** User `admin` (Shaziya Shaikh)
- **Commit Checked Out:** `1297a48` (`fix(docker): strip Windows CRLF from mvnw during Docker image build`)
- **Stage Results Breakdown:**
  1. **Checkout:** **SUCCESS** (Local git clone completed in 2s).
  2. **Environment Check:** **SUCCESS** (Java 21, Maven 3.9.16, Docker 27.2.0 verified).
  3. **Build:** **SUCCESS** (Maven clean compile clean exit).
  4. **Unit Tests:** **SUCCESS** (33 tests run, 0 failures, 0 errors, 0 skipped; JUnit reports parsed).
  5. **Package:** **SUCCESS** (Generated `target/public-parking-dashboard-1.0.0.jar` archived).
  6. **Selenium Tests:** **SKIPPED** (Condition `params.RUN_SELENIUM == false`).
  7. **Docker Build:** **SUCCESS** (Built multi-stage image `public-parking-dashboard:1.0.0-12`).
  8. **Docker Push:** **SKIPPED** (Condition `params.PUSH_IMAGE == false`).
  9. **Deploy:** **SUCCESS** (Started container on port 8085).
  10. **Declarative Post Actions:** **SUCCESS** (Workspace cleaned, banner logged).
- **Final Result:** **SUCCESS**

---

## 7. Screenshots as Evidence
- **Jenkins Pipeline Stage View (Build #12 SUCCESS):**  
  ![Jenkins Pipeline Success](screenshots/task_12/jenkins_pipeline_success.png)  
  *Evidence Description:* Live screenshot of Jenkins Web UI (`http://localhost:8080/job/Public-Parking-DevOps/`) displaying the Pipeline Stage View with Build #12 passing all stages cleanly in green.

---

## 8. Problems Encountered and Solutions
1. **SECURITY-2824 Local Git Checkout Rejection:**  
   *Root Cause:* Jenkins Git plugin blocks local `file:///` URLs by default to prevent directory traversal.  
   *Solution:* Activated `System.setProperty("hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT", "true")` via Script Console and created persistent init script.
2. **Missing `sh` Binary on Windows Controller:**  
   *Root Cause:* Jenkins default `sh` step cannot run on Windows without Cygwin/Git Bash in system PATH.  
   *Solution:* Implemented `isUnix() ? sh(...) : bat(...)` branching in `Jenkinsfile`.
3. **CRLF Line Endings in `mvnw` inside Alpine Linux Container:**  
   *Root Cause:* Windows Git checkout introduced `\r\n` line endings into `mvnw`, causing Alpine's BusyBox kernel to search for `/bin/sh\r`.  
   *Solution:* Added `RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw` to `Dockerfile`.
4. **Port 8080 Collision with Jenkins Controller:**  
   *Root Cause:* Jenkins runs on host port 8080. Deploying a container mapping `-p 8080:8080` fails due to port conflict.  
   *Solution:* Configured pipeline parameter `APP_PORT` to default to `8085`.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. The CI/CD pipeline builds, tests, packages, containerizes, and deploys the application with 100% success on the live Jenkins controller.
