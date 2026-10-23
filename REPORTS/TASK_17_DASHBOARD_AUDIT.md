# TASK 17: Dashboard Audit, Data Provenance, and Enhancement Report

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | **Roll Number:** 24102A2005  
**Execution Date:** October 18, 2026  
**Status:** **AUDITED, ENHANCED & VERIFIED** (45/45 Unit/Integration Tests Passing, 6/6 Python Tests Passing)

---

## Executive Summary

This report documents the comprehensive audit and enhancement of the ParkWatch public parking dashboard following the integration of the AI computer-vision module. In accordance with strict DevOps and academic safety rules:
1. Baseline operational metrics were measured from the running database without modifying or resetting `data/parkingdb.mv.db`.
2. The root cause of the **24-versus-25 parking locations discrepancy** was thoroughly investigated and identified: the persistent database originally seeded 24 pan-Indian metropolitan locations, and location #25 (`PPL-099 Civic Center Innovation Garage`) was dynamically created during the automated endpoint regression suite in Task 14 (`scripts/test-endpoints.ps1`). The database seed guard (`if (repo.count() > 0) return;`) rightly prevented subsequent overwrites.
3. A transparent **Data Provenance Audit** was performed: while source code in `SampleDataConfig.java` was previously drafted for 24 Mumbai BMC lots, the active persistent database actually serves an illustrative pan-Indian metropolitan sample (Mumbai, Delhi NCR, Bengaluru, Hyderabad, Chennai, Kolkata, Pune, Ahmedabad, Chandigarh). Real-time telemetry is simulated for academic demonstration.
4. Three high-value, safe improvements were implemented:
   - **Fixed Hibernate Lazy Proxy JSON Serialization Error:** Eliminated an HTTP 500 failure on `GET /api/v1/events/recent` and `GET /api/v1/events/location/{id}` by adding `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`.
   - **Global Event Audit Log View:** Created `@GetMapping("/events")` in `ParkingEventController` and enhanced `event-list.html` to provide an accessible audit log across all municipal facilities with direct navigation.
   - **CSV / Data Export Endpoint:** Implemented `GET /api/v1/locations/export?format=csv` and integrated an **Export CSV** action on the main dashboard for administrative reporting.
   - **Data Freshness Indicators:** Embedded real-time telemetry delay indicators (`>60 mins`) on the dashboard table rows.
5. Full regression test suites passed (45/45 Maven Spring Boot tests, 6/6 Python AI pipeline tests).

---

## 1. Verified Baseline Operational Metrics

The application was inspected running on dedicated test port **8085** (preserving the active Jenkins CI service on port 8080). Operational metrics were queried directly from the REST API:

| Metric | Measured Value | API Endpoint / Source |
|:---|:---:|:---|
| **Total Parking Locations** | `25` | `GET /api/v1/locations` |
| **Total Parking Capacity** | `8,040` spaces | `GET /api/v1/dashboard/summary` |
| **Total Occupied Spaces** | `4,531` spaces | `GET /api/v1/dashboard/summary` |
| **Total Available Spaces** | `3,509` spaces | `GET /api/v1/dashboard/summary` |
| **Overall Utilization Rate** | `56.4%` | `GET /api/v1/dashboard/summary` |
| **Full Capacity Locations** | `3` (`100%` saturation) | `GET /api/v1/locations?filter=FULL` |
| **Low Availability Locations** | `5` (`>= 85%` utilization) | `GET /api/v1/locations?filter=LOW` |
| **Available / Normal Locations** | `17` (`< 85%` utilization) | `GET /api/v1/locations?filter=AVAILABLE` |
| **Stale Telemetry Locations** | `25` (`> 60 mins` old) | `GET /api/v1/locations?filter=STALE` |
| **Total System Alerts** | `33` (3 Full + 5 Low + 25 Stale) | `GET /api/v1/alerts` |
| **Recent Parking Events** | `1` recorded event in DB | `GET /api/v1/events/recent` |

---

## 2. Investigation: The 24-versus-25 Location Discrepancy

### 2.1 The Discrepancy
Earlier project documentation (Tasks 03, 04, and 15) reported **24** parking locations, whereas recent verification tasks reported **25** locations.

### 2.2 Root Cause Analysis
An inspection of the database records and git commit history revealed the exact chain of events:
1. **Initial Seeding (Task 03 / Commit `53f42f9d`):**  
   The application seeded 24 initial locations (`PPL-001` through `PPL-024`) into the persistent file-backed H2 database at `data/parkingdb.mv.db`.
2. **Automated Endpoint Testing (Task 14 / Commit `d40dae0a`):**  
   The test script [`scripts/test-endpoints.ps1`](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/scripts/test-endpoints.ps1) lines 40–54 performed a functional test of the `POST /api/v1/locations` endpoint:
   ```powershell
   $newLocBody = @{
       name = "Civic Center Innovation Garage"
       area = "Cyber City"
       address = "500 Tech Boulevard"
       totalCapacity = 100
       occupiedSpaces = 25
       locationCode = "PPL-099"
       contactPhone = "+91 98765 43210"
       description = "Smart automated municipal lot"
       active = $true
   } | ConvertTo-Json
   $created = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/locations" -Method Post -Body $newLocBody ...
   ```
   This created **Location #25** (`PPL-099 Civic Center Innovation Garage`).
3. **Database Guard Preservation:**  
   In `SampleDataConfig.java`, line 35 enforces:
   ```java
   if (repo.count() > 0) {
       log.info("Parking location database already initialized ({} locations present). Skipping seed.", repo.count());
       return;
   }
   ```
   Because `data/parkingdb.mv.db` is a persistent database file and strict project rules forbid deleting or resetting `parkingdb.mv.db`, the 25th record persisted across all subsequent test runs and restarts.

### 2.3 Verdict
The difference is **not** a database corruption or a bug; it is a **legitimate persistent test entity** created during end-to-end integration testing in Task 14. In adherence to the safety rule *"Do not delete or modify any records to make the numbers match"*, the record is preserved as an active demonstration facility.

---

## 3. Data Provenance Audit

An audit of the active records in `data/parkingdb.mv.db` establishes the true provenance of the dashboard dataset:

| ID | Location Code | Facility Name | Geographic Locality | Metro Region | Provenance Classification |
|:---:|:---:|:---|:---|:---|:---|
| 1 | `PPL-001` | Bandra West Civic Plaza Parking | Bandra West, SV Road | Mumbai (MMR) | Illustrative Sample Data |
| 2 | `PPL-002` | Koramangala IT Park Parking | Koramangala 4th Block | Bengaluru | Illustrative Sample Data |
| 3 | `PPL-003` | Sector 17 Government Parking | Sector 17 Plaza | Chandigarh | Illustrative Sample Data |
| 4 | `PPL-004` | Connaught Place Underground Parking | Block A, Connaught Place | New Delhi (NCR) | Illustrative Sample Data |
| 5 | `PPL-005` | Jubilee Hills Club Parking | Road No. 36, Jubilee Hills | Hyderabad | Illustrative Sample Data |
| 6 | `PPL-006` | Vashi Railway Station Parking | Sector 17, Vashi | Navi Mumbai (MMR) | Illustrative Sample Data |
| 7 | `PPL-007` | MG Road Metro Parking | MG Road Station | Bengaluru | Illustrative Sample Data |
| 8 | `PPL-008` | Salt Lake Sector V Parking | Webel More, Sector V | Kolkata | Illustrative Sample Data |
| 9 | `PPL-009` | Garware College Road Parking | FC Road, Shivajinagar | Pune | Illustrative Sample Data |
| 10 | `PPL-010` | Anna Nagar Tower Parking | 3rd Avenue, Anna Nagar West | Chennai | Illustrative Sample Data |
| 11 | `PPL-011` | Phoenix MarketCity Parking - Block C | Whitefield Main Road | Bengaluru | Commercial Retail Sample |
| 12 | `PPL-012` | Linking Road Shopping Parking | Linking Road, Bandra West | Mumbai (MMR) | Illustrative Sample Data |
| 13 | `PPL-013` | Laxmi Nagar District Centre Parking | District Centre, Laxmi Nagar | New Delhi (NCR) | Illustrative Sample Data |
| 14 | `PPL-014` | Hitech City Central Parking | Cyber Towers, Hitech City | Hyderabad | Commercial Tech Hub Sample |
| 15 | `PPL-015` | Law Garden Night Market Parking | Law Garden Road, Ellisbridge | Ahmedabad | Illustrative Sample Data |
| 16 | `PPL-016` | Sarojini Nagar Market Parking | Sarojini Nagar Market | New Delhi (NCR) | Municipal Market Sample |
| 17 | `PPL-017` | Dadar Station East Parking | Dadar East | Mumbai (MMR) | Railway Transit Sample |
| 18 | `PPL-018` | Commercial Street Parking | Commercial Street | Bengaluru | Commercial Retail Sample |
| 19 | `PPL-019` | Churchgate Station Parking | Marine Lines | Mumbai (MMR) | Railway Transit Sample |
| 20 | `PPL-020` | Karol Bagh Multi-Level Parking | Ajmal Khan Road | New Delhi (NCR) | Municipal Lot Sample |
| 21 | `PPL-021` | Egmore Bus Terminus Parking | EVR Periyar Salai | Chennai | Transit Hub Sample |
| 22 | `PPL-022` | Majestic Bus Stand Parking | Kempegowda Bus Station | Bengaluru | Transit Hub Sample |
| 23 | `PPL-023` | Sector 18 Noida Parking | Sector 18 Commercial Hub | Noida (NCR) | Commercial Center Sample |
| 24 | `PPL-024` | Vastrapur Lake Parking | Vastrapur Lake Road | Ahmedabad | Civic Amenity Sample |
| 25 | `PPL-099` | Civic Center Innovation Garage | 500 Tech Boulevard, Cyber City | Test Facility | Dynamic Integration Test Entity |

### Provenance Audit Conclusions:
- **Pan-Indian Metropolitan Scope:** The active database does not contain purely BMC Mumbai municipal lots; it represents a diverse cross-section of 9 Indian urban hubs across 8 states/UTs.
- **Simulated Real-Time Feeds:** Brihanmumbai Municipal Corporation (BMC/MCGM) does not expose an open real-time sensor API for private third-party ingestion. Therefore, all occupancy numbers, timestamps, and sensor telemetry represent simulated operational data for DevOps testing and academic demonstration.
- **AI Separation:** AI computer-vision predictions from `POST /ai-analysis` (derived from the Brazilian PKLot dataset) remain strictly decoupled from this database and never alter municipal lot counts.

---

## 4. Dashboard Feature Audit & Testing

| Feature Under Audit | Tested Endpoint / Route | Verification Command / Method | Status | Audit Findings |
|:---|:---|:---|:---:|:---|
| **Search Functionality** | `GET /?search=Bandra` & `GET /api/v1/locations?search=Bandra` | `curl.exe -s "http://localhost:8085/?search=Bandra"` | **WORKING** | Correctly filters and returns 2 matching facilities (Bandra Civic Plaza, Linking Road). |
| **Status Filtering** | `GET /?filter=FULL` & `GET /api/v1/locations?filter=...` | REST API calls with `FULL`, `LOW`, `AVAILABLE`, `STALE` | **WORKING** | Correctly partitions locations into `FULL` (3), `LOW` (5), `AVAILABLE` (17), `STALE` (25). |
| **Location Details** | `GET /locations/1` | `Invoke-WebRequest -Uri 'http://localhost:8085/locations/1'` | **WORKING** | HTTP 200; displays capacity progress, contact phone, timestamps, and action buttons. |
| **Add / Edit Facilities** | `GET /locations/new` & `GET /locations/1/edit` | HTTP GET and validation tests | **WORKING** | HTTP 200; forms validate capacity $\ge 1$, occupied $\le$ capacity, and required strings. |
| **Occupancy Boundary Patch** | `PATCH /api/v1/locations/1/occupancy` | Tested boundary payload (`occupiedSpaces > totalCapacity`) | **WORKING** | Correctly rejected with HTTP 400 Bad Request; valid occupancies update in place. |
| **Recent Event History** | `GET /api/v1/events/recent` | `curl.exe -s http://localhost:8085/api/v1/events/recent` | **FIXED** | Previously failed with HTTP 500 (Jackson lazy proxy bug). Now returns HTTP 200 with JSON array. |
| **Global Audit Log UI** | `GET /events` | `curl.exe -s http://localhost:8085/events` | **NEW** | Rendered global event audit log with facility links and operator notes. |
| **Alerts & Exceptions** | `GET /alerts` & `GET /api/v1/alerts` | `Invoke-RestMethod -Uri 'http://localhost:8085/api/v1/alerts'` | **WORKING** | HTTP 200; returns 33 alerts (3 Full, 5 Low Availability, 25 Stale Telemetry). |
| **AI Stall Analysis** | `GET /ai-analysis` & `POST /ai-analysis` | End-to-end inference using YOLOv8n checkpoint | **WORKING** | HTTP 200; 166 stalls detected, annotated JPEG served via `/ai-analysis/image/...`. |
| **CSV Data Export** | `GET /api/v1/locations/export?format=csv` | `curl.exe -s http://localhost:8085/api/v1/locations/export` | **NEW** | HTTP 200; exports all 25 facilities with full metadata in CSV format. |

---

## 5. Improvements Implemented

Three safe, architectural improvements were introduced to strengthen the dashboard without altering the existing schema, database, or model:

### 1. Fixed Hibernate Lazy Proxy JSON Serialization
- **Issue:** Calling `GET /api/v1/events/recent` or `GET /api/v1/events/location/{id}` triggered `com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor`.
- **Solution:** Added `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` to [ParkingLocation.java](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/java/com/parking/dashboard/model/ParkingLocation.java) and [ParkingEvent.java](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/java/com/parking/dashboard/model/ParkingEvent.java).
- **Result:** Event REST APIs now serialize clean JSON records with nested location details and zero exceptions.

### 2. Global Event Audit Log & Navigation
- **Issue:** Users could record events via `/events/new` or view events for a single facility via `/events/location/{id}`, but there was no global audit view across all municipal lots.
- **Solution:** Added `@GetMapping` in [ParkingEventController.java](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/java/com/parking/dashboard/controller/ParkingEventController.java), enhanced [event-list.html](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/resources/templates/event-list.html) to support both global and per-facility listings, and added **Events Log** to the navigation bar in [header.html](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/resources/templates/fragments/header.html).

### 3. Municipal Data Export (CSV) & Freshness Indicators
- **Issue:** No export capability existed for administrative reporting, and row-level data staleness lacked visual prominence.
- **Solution:** 
  1. Implemented `GET /api/v1/locations/export` in [ParkingApiController.java](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/java/com/parking/dashboard/controller/ParkingApiController.java) with RFC 4180 CSV escaping and `attachment; filename="parkwatch_locations.csv"`.
  2. Added an **Export CSV** button and direct link on [dashboard.html](file:///c:/Users/Admin/Desktop/Public-Parking-DevOps/src/main/resources/templates/dashboard.html).
  3. Added data freshness warning badges (`bi-clock-history`) to the **Last Updated** column for rows exceeding the 60-minute threshold.

---

## 6. Verification and Regression Testing

### 6.1 Unit and Integration Test Results
Executed: `mvn test -Dtest="!*Selenium*"`

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.parking.dashboard.controller.AiAnalysisControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.20 s
[INFO] Running com.parking.dashboard.controller.ParkingApiControllerTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.669 s
[INFO] Running com.parking.dashboard.controller.ParkingEventControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.202 s
[INFO] Running com.parking.dashboard.ParkingDashboardApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.544 s
[INFO] Running com.parking.dashboard.service.ParkingLocationServiceTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.263 s
[INFO] 
[INFO] Results:
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
**Total:** **45 passed, 0 failures, 0 errors** (6 new integration tests added for CSV export, recent event serialization, and global audit log rendering).

### 6.2 AI Pipeline Regression Tests
Executed: `python tests/test_ai_pipeline.py`
```text
Ran 6 tests in 0.079s
OK
```
**Total:** **6 passed, 0 failures**.

---

## 7. Known Limitations and Transparency Disclosures

1. **Simulated Telemetry:** Sensor feeds and occupancy numbers remain simulated for academic demonstration. BMC does not provide an open public sensor API for real-time occupancy.
2. **Pilot AI Model:** The YOLOv8n computer vision model was trained for 1 epoch on 174 subsampled PKLot images ($mAP_{50} = 0.0063$, Precision = $0.0087$, Recall = $0.0500$) and is strictly designated as an experimental academic demonstration.
3. **Port 8080 Utilization:** Port 8080 is reserved by the Jenkins CI service (PID 7180) on the host machine. The dashboard application safely binds to configured ports (e.g. 8081 or 8085).

---

## 8. Git Safety Check

Executed: `git status`
- **Staged files:** None (`no changes added to commit`).
- **Unstaged changes:** Only the audited and enhanced files.
- **Untracked files:** Only new report artifacts, documentation, and the new unit test file.
- **Safety Compliance:** Zero database files (`*.mv.db`), weights (`*.pt`), dataset images, or secrets were committed or pushed.
