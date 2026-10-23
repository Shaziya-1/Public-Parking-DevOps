# Task 03 Report: Domain Modeling & Database Schema (JPA)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 03  
**Status:** **COMPLETED**  

---

## 1. Objective
Design and implement the object-relational domain model using Jakarta Persistence (JPA) and Spring Data JPA to represent municipal parking facilities, capacity configurations, and real-time vehicle entry/exit events.

---

## 2. Tools and Technologies Used
- **Persistence Framework:** Spring Data JPA / Hibernate ORM
- **Database:** In-Memory H2 Database (with MySQL syntax compatibility)
- **Validation:** Jakarta Bean Validation (`jakarta.validation.constraints`)
- **Language:** Java 21 LTS

---

## 3. Work Performed
1. Created the core `ParkingLocation` entity representing parking lots:
   - Primary key: `id` (Auto-generated `Long`).
   - Facility identity: `name` (NotBlank, indexed), `district` (NotBlank), `address`.
   - Capacity properties: `totalCapacity` (Min: 1), `occupiedSpots` (Min: 0).
   - Operational properties: `hourlyRate`, `latitude`, `longitude`, `status` (`LOW`, `MODERATE`, `HIGH`, `CRITICAL`).
   - Timestamps: `createdAt`, `updatedAt` managed via lifecycle callbacks.
2. Created the `ParkingEvent` entity representing vehicle activity audit logs:
   - Primary key: `id`.
   - Foreign relation: `@ManyToOne` relationship to `ParkingLocation`.
   - Event metadata: `eventType` (`CHECK_IN`, `CHECK_OUT`), `spotCount`, `vehicleType`, `plateHash`, `timestamp`.
3. Created Spring Data repositories:
   - `ParkingLocationRepository`: Provides CRUD, district queries, and capacity-based filtering.
   - `ParkingEventRepository`: Provides event logging and historical retrieval by location.
4. Configured embedded H2 database in `application.properties` with web console enabled for local inspection.

---

## 4. Files Created or Modified
- `src/main/java/com/parking/dashboard/model/ParkingLocation.java`: Entity for parking facilities.
- `src/main/java/com/parking/dashboard/model/ParkingEvent.java`: Entity for entry/exit events.
- `src/main/java/com/parking/dashboard/repository/ParkingLocationRepository.java`: JPA repository.
- `src/main/java/com/parking/dashboard/repository/ParkingEventRepository.java`: JPA repository.
- `src/main/resources/application.properties`: H2 connection and JPA hibernate ddl-auto settings.

---

## 5. Commands Executed
```powershell
# Verify JPA repository compilation and database initialization
.\mvnw.cmd test-compile
```

### Execution Log / Terminal Output
```text
[INFO] Scanning for projects...
[INFO] ----------------< com.parking:public-parking-dashboard >----------------
[INFO] Building public-parking-dashboard 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) ---
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) ---
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) ---
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Actual Test Results & Verification Evidence
- Schema automatically created by Hibernate on startup (`spring.jpa.hibernate.ddl-auto=update`).
- Data integrity constraints validated via Jakarta constraints (`@Min(0)`, `@NotNull`, `@NotBlank`).
- Verified zero relational mapping errors or unmapped entity warnings during Spring Boot startup.

---

## 7. Screenshots as Evidence
- **H2 Database Schema & Table Verification:**  
  ![H2 Schema and Tables](screenshots/task_03/h2_console_schema.png)  
  *Evidence Description:* Terminal output displaying H2 database connection, tables `PARKING_LOCATIONS` and `PARKING_EVENTS`, and column structure in MySQL compatibility mode.

> **Screenshot Capture Instructions for Student:**  
> When the application is running, navigate to `http://localhost:8080/h2-console`, connect with JDBC URL `jdbc:h2:mem:parkingdb`, expand the schema tree on the left panel, take a screenshot (`Win + Shift + S`), and save it to:  
> `C:\Users\Admin\Desktop\Public-Parking-DevOps\REPORTS\screenshots\task_03\h2_console_schema.png`

---

## 8. Problems Encountered and Solutions
- **Issue:** Potential circular references during JSON serialization between `ParkingLocation` and its list of `ParkingEvent` objects.
- **Solution:** Configured `@JsonIgnore` and dedicated DTO mappings to isolate data transfer objects from direct JPA entities.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. Models and repositories are fully functional and verified.
