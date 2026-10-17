# Task 11 Report: Reverse Proxy Configuration (Nginx) & Network Architecture

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 11  
**Status:** **COMPLETED**  

---

## 1. Objective
Design and configure a production reverse proxy architecture using Nginx and Docker Compose to expose standard HTTP traffic on port 80, terminate external connections, forward proxy headers, secure internal administrative endpoints, and route traffic to the containerized Spring Boot application running on an isolated bridge network.

---

## 2. Tools and Technologies Used
- **Reverse Proxy:** Nginx 1.25 Alpine (`nginx:alpine`)
- **Container Engine & Orchestration:** Docker Compose v2 with Docker Desktop engine 27.2.0
- **Networking:** Docker user-defined bridge network (`parkwatch-network`)
- **Backend Service:** Spring Boot 3.3.4 container (`parkwatch-app`) exposing internal port 8080

---

## 3. Work Performed
1. Configured `docker/nginx.conf`:
   - Defined upstream cluster `parkwatch-backend` pointing to container hostname `app:8080`. (Avoided underscores in upstream name to prevent RFC 7230 Host header rejection by Tomcat).
   - Configured proxy headers:
     - `Host $host`
     - `X-Real-IP $remote_addr`
     - `X-Forwarded-For $proxy_add_x_forwarded_for`
     - `X-Forwarded-Proto $scheme`
   - Added security restrictions: blocked external access to sensitive internal paths (`/h2-console`, `/actuator/env`, `/actuator/beans`) returning HTTP 403 Forbidden.
   - Configured public `/health` endpoint mapping to internal `/actuator/health` with HTTP status caching.
2. Authored `docker-compose.yml`:
   - Service `app`: builds from local Dockerfile, sets `SPRING_PROFILES_ACTIVE=prod`, exposes internal port 8080 (mapped to host 8081 to avoid host collision with Jenkins on 8080), with healthcheck probing `http://localhost:8080/actuator/health`.
   - Service `nginx`: mounts `docker/nginx.conf` read-only, publishes host port `80:80`, depends on `app` with condition `service_healthy`.
   - Isolated bridge network `parkwatch-network`.
3. Executed `docker compose up -d` and verified multi-container orchestration.
4. Validated reverse proxy routing, header forwarding, and security blocking via `curl.exe` and browser tests.

---

## 4. Files Created or Modified
- `docker/nginx.conf`: Nginx reverse proxy configuration.
- `docker-compose.yml`: Multi-container orchestration definition with healthchecks and network.
- `REPORTS/screenshots/task_11/nginx_proxy_running.png`: Live screenshot of dashboard rendered via Nginx port 80.

---

## 5. Commands Executed
```powershell
# Deploy the multi-container stack in detached mode
docker compose up -d

# Verify container status and health
docker compose ps

# Test reverse proxy root routing (port 80)
curl.exe -i http://localhost:80/

# Test public health endpoint via Nginx
curl.exe -i http://localhost:80/health

# Verify security blocking of sensitive paths
curl.exe -i http://localhost:80/h2-console
curl.exe -i http://localhost:80/actuator/env
```

### Execution Log / Terminal Output
```text
PS C:\Users\Admin\Desktop\Public-Parking-DevOps> docker compose ps
NAME              IMAGE                              COMMAND                  SERVICE   CREATED          STATUS                    PORTS
parkwatch-app     public-parking-devops-app:latest   "sh -c 'java $JAVA_O…"   app       20 minutes ago   Up 20 minutes (healthy)   0.0.0.0:8081->8080/tcp
parkwatch-nginx   nginx:alpine                       "/docker-entrypoint.…"   nginx     20 minutes ago   Up 20 minutes (healthy)   0.0.0.0:80->80/tcp

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> curl.exe -s http://localhost:80/health
{"status":"UP"}

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> curl.exe -s -o /dev/null -w "%{http_code}" http://localhost:80/h2-console
403

PS C:\Users\Admin\Desktop\Public-Parking-DevOps> curl.exe -s http://localhost:80/api/v1/dashboard/summary
{"totalLocations":24,"totalCapacity":16846,"totalOccupied":7225,"totalAvailable":9621,"occupancyPercentage":42.89,"fullLocations":3,"lowAvailabilityLocations":5,"staleLocations":4}
```

---

## 6. Actual Test Results & Verification Evidence
- **Container Health:** Both `parkwatch-app` and `parkwatch-nginx` are running and reporting `healthy`.
- **Port 80 Routing:** External HTTP requests on port 80 are accepted by Nginx and seamlessly forwarded to Spring Boot backend.
- **Data Integrity:** All 24 BMC municipal parking facilities, summary aggregates (16,846 total capacity), and charts render cleanly through the proxy.
- **Security Validation:** Direct external requests to `/h2-console` and `/actuator/env` are intercepted by Nginx and return `403 Forbidden`.
- **Zero Host Collision:** Port 8080 collision with Jenkins service resolved by internal network routing and optional host port 8081 mapping.

---

## 7. Screenshots as Evidence
- **Nginx Reverse Proxy & Dashboard on Port 80:**  
  ![Nginx Reverse Proxy Running](screenshots/task_11/nginx_proxy_running.png)  
  *Evidence Description:* Live browser capture of `http://localhost:80` showing the ParkWatch dashboard with 24 municipal locations, capacity statistics, and live health status served through Nginx reverse proxy.

---

## 8. Problems Encountered and Solutions
1. **Host Port Collision on 8080:** Jenkins is running on host port 8080. If Docker Compose attempts to bind `8080:8080`, binding fails.  
   *Solution:* Published backend container as `8081:8080` for diagnostic access and bound Nginx to `80:80`.
2. **Tomcat RFC 7230 Underscore Rejection:** The original upstream name `parking_app` caused Tomcat 10.1 to reject incoming proxy requests with `HTTP 400 Bad Request` because RFC 7230 forbids underscores in Host headers.  
   *Solution:* Renamed upstream to `parkwatch-backend` and configured Nginx `proxy_set_header Host $host`.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. Multi-container proxy architecture is fully deployed, healthy, and operational.
