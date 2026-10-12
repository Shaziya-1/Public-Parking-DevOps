# DevOps Troubleshooting Guide

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  

---

## 1. Port 8080 Already in Use

### Symptom:
`Port 8080 was already in use. Web server failed to start.`

### Root Cause:
Another instance of Tomcat, Jenkins, or a background Java process is listening on TCP port 8080.

### Resolution:
**On Windows (PowerShell):**
```powershell
# Identify process using port 8080
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess

# Terminate process by PID (e.g., PID 1234)
Stop-Process -Id 1234 -Force
```

**Alternative:** Change application port dynamically in `application.properties` or CLI:
```bash
java -jar target/public-parking-dashboard-1.0.0.jar --server.port=8081
```

---

## 2. Selenium WebDriver Mismatch or Network Download Restriction

### Symptom:
`NoSuchDriverException: Unable to obtain: msedgedriver` or `UnknownHostException: msedgedriver.azureedge.net`

### Root Cause:
Workstation or CI sandbox is offline, firewalled, or lacks direct outbound HTTP access to cloud driver CDNs.

### Resolution:
1. Ensure the application is actively running on `http://localhost:8080`.
2. Specify a local driver binary via JVM property:
   ```bash
   mvn test "-Dtest=ParkingDashboardSeleniumTest" "-Dwebdriver.edge.driver=C:\path\to\msedgedriver.exe"
   ```
3. In isolated / offline environments, the test suite uses JUnit 5 Assumptions to log diagnostics and gracefully bypass browser launch without halting the primary CI pipeline build.

---

## 3. In-Memory Database State Reset

### Symptom:
Newly created parking locations disappear after restarting the application.

### Root Cause:
The application uses an in-memory H2 database (`jdbc:h2:mem:parkingdb`), which is designed to re-initialize from scratch on every process boot for clean demonstration and isolation.

### Resolution:
To persist data across application restarts, switch to a file-based H2 database in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/parkingdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
```

---

## 4. Docker Daemon Unreachable

### Symptom:
`error during connect: This error may indicate that the docker daemon is not running.`

### Root Cause:
Docker Desktop service is stopped or WSL2 backend is not started.

### Resolution:
1. Launch **Docker Desktop** from the Windows Start menu.
2. Wait until the whale icon in the taskbar status shows **Docker Desktop is running**.
3. Verify from PowerShell:
   ```powershell
   docker version
   docker info
   ```

---

## 5. Ansible SSH Authentication Failure

### Symptom:
`UNREACHABLE! => {"msg": "Failed to connect to the host via ssh: Permission denied (publickey)."}`

### Root Cause:
Target host does not have the control node's public key in `~/.ssh/authorized_keys`, or permissions on the SSH private key are too open.

### Resolution:
1. Check permissions on your private key:
   ```bash
   chmod 600 ~/.ssh/id_rsa
   ```
2. Copy your public key to the target host:
   ```bash
   ssh-copy-id -i ~/.ssh/id_rsa.pub ubuntu@<target-ip>
   ```
3. Test direct SSH login:
   ```bash
   ssh -i ~/.ssh/id_rsa ubuntu@<target-ip>
   ```
