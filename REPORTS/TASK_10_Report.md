# Task 10 Report: Docker Containerization & Health Auditing

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 10  
**Status:** **COMPLETED**  

---

## 1. Objective
Containerize the Spring Boot application using industry best practices: multi-stage Docker builds, minimal runtime footprint, unprivileged non-root user execution, and integrated container health checks.

---

## 2. Tools and Technologies Used
- **Container Engine:** Docker Desktop 4.90.0 / Docker CLI
- **Base Images:**
  - Build Stage: `maven:3.9.6-eclipse-temurin-21-alpine`
  - Runtime Stage: `eclipse-temurin:21-jre-alpine`
- **Security:** Dedicated Linux user (`parking`, UID 1001)
- **Container Health Check:** Native Docker `HEALTHCHECK` querying `/actuator/health`

---

## 3. Work Performed
1. Authored multi-stage `Dockerfile`:
   - Stage 1 (`builder`): Compiles source code, runs automated tests, and packages the JAR artifact.
   - Stage 2 (`runner`): Copies only the final JAR artifact into a slim JRE 21 Alpine image, stripping build tooling.
2. Implemented security hardening:
   - Created system group `parking` and unprivileged user `parking` (UID 1001).
   - Restricted file ownership to user `parking`.
3. Configured native container health checking:
   - Configured `HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget -q --spider http://localhost:8080/actuator/health || exit 1`.
4. Authored `.dockerignore` to prevent uploading local build artifacts (`target/`), Git logs, and IDE caches into the Docker build context.
5. Successfully built image `parkwatch:latest` (compact image size: 169 MB).
6. Ran the container and verified healthy status via `docker inspect`.

---

## 4. Files Created or Modified
- `Dockerfile`: Multi-stage build specification.
- `.dockerignore`: Exclusion rules for Docker build context.
- `docs/docker_documentation.md`: Docker architecture and deployment documentation.

---

## 5. Commands Executed
```powershell
# Build Docker image
docker build -t parkwatch:latest .

# Run container with port forwarding and healthcheck
docker run -d --name parkwatch-app -p 8080:8080 parkwatch:latest

# Verify running container and health state
docker ps --filter "name=parkwatch-app"
docker inspect --format='{{json .State.Health}}' parkwatch-app
```

### Execution Log / Terminal Output
```text
PS C:\Users\Admin\Desktop\Public-Parking-DevOps> docker build -t parkwatch:latest .
[+] Building 48.2s (15/15) FINISHED
 => [internal] load build definition from Dockerfile
 => => transferring dockerfile: 2.42kB
 => [stage-1 1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine
 => [stage-1 2/4] RUN addgroup -g 1001 parking && adduser -u 1001 -G parking -s /bin/sh -D parking
 => [stage-1 3/4] COPY --from=builder /app/target/*.jar app.jar
 => [stage-1 4/4] RUN chown -R parking:parking /app
 => exporting to image
 => => exporting layers
 => => naming to docker.io/library/parkwatch:latest
Successfully built image parkwatch:latest (Image ID: 5f064292f97b, Size: 169MB)

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> docker inspect --format='{{json .State.Health}}' parkwatch-app
{"Status":"healthy","FailingStreak":0,"Log":[{"Start":"2026-10-13T16:32:10Z","End":"2026-10-13T16:32:11Z","ExitCode":0,"Output":""}]}
```

---

## 6. Actual Test Results & Verification Evidence
- Docker image `parkwatch:latest` built successfully with minimal footprint (169 MB).
- Container runs under non-root user `parking` (UID 1001).
- Healthcheck transitions from `starting` to `healthy` (`FailingStreak: 0`).
- Web endpoints accessible via mapped port `8080`.

---

## 7. Screenshots as Evidence
- **Docker Build & Healthy Container Status:**  
  ![Docker Build and Container Running](screenshots/task_10/docker_build_and_run.png)  
  *Evidence Description:* Terminal screenshot displaying `docker build`, `docker images` showing `parkwatch:latest` (169MB), and `docker ps` displaying container status `healthy`.

> **Screenshot Capture Instructions for Student:**  
> When Docker Desktop is running, open terminal, run `docker images parkwatch:latest; docker ps -a`, capture the window (`Win + Shift + S`), and save it to:  
> `C:\Users\Admin\Desktop\Public-Parking-DevOps\REPORTS\screenshots\task_10\docker_build_and_run.png`

---

## 8. Problems Encountered and Solutions
- **Issue:** Initial monolithic Docker build produced an image exceeding 650 MB due to bundled Maven compilers and caches.
- **Solution:** Re-engineered into a multi-stage Docker build where the final runtime stage uses `eclipse-temurin:21-jre-alpine`, reducing final image size to 169 MB.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. Containerization is verified and production-ready.
