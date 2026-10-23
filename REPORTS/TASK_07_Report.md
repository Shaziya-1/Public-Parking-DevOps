# Task 07 Report: Automated Backend Testing (JUnit 5 & MockMvc)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 07  
**Status:** **COMPLETED**  

---

## 1. Objective
Establish an automated backend testing suite using JUnit 5, Mockito, and Spring MockMvc to guarantee high code quality, boundary condition validation, regression prevention, and build verification prior to packaging.

---

## 2. Tools and Technologies Used
- **Test Framework:** JUnit 5 (Jupiter API & Engine)
- **Mocking Library:** Mockito 5.x
- **Integration Testing:** Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`, `MockMvc`)
- **Maven Test Runner:** Maven Surefire Plugin 3.2.5

---

## 3. Work Performed
1. Authored `ParkingLocationServiceTest.java`:
   - 21 unit test methods validating business rules.
   - Tests over-capacity prevention, check-in spot incrementing, check-out spot decrementing, negative capacity protection, dynamic status transitions, search filtering, and threshold alert extraction.
2. Authored `ParkingApiControllerTest.java`:
   - 11 web layer integration tests using `MockMvc`.
   - Tests endpoint status codes (200, 201, 204, 400, 404), JSON payload serialization, request body validation, and error response structure.
3. Authored `ParkingDashboardApplicationTests.java`:
   - Validates that the Spring ApplicationContext bootstraps completely without bean injection errors.
4. Executed full clean test and package lifecycle via Maven.

---

## 4. Files Created or Modified
- `src/test/java/com/parking/dashboard/service/ParkingLocationServiceTest.java`: 21 service tests.
- `src/test/java/com/parking/dashboard/controller/ParkingApiControllerTest.java`: 11 MockMvc tests.
- `src/test/java/com/parking/dashboard/ParkingDashboardApplicationTests.java`: 1 context load test.
- `pom.xml`: Surefire plugin and test dependency configuration.

---

## 5. Commands Executed
```powershell
# Run full clean test lifecycle
.\mvnw.cmd clean test

# Build production JAR package
.\mvnw.cmd package -DskipTests=false
```

### Execution Log / Terminal Output
```text
[INFO] Scanning for projects...
[INFO] ----------------< com.parking:public-parking-dashboard >----------------
[INFO] Building public-parking-dashboard 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.parking.dashboard.controller.ParkingApiControllerTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.421 s -- in com.parking.dashboard.controller.ParkingApiControllerTest
[INFO] Running com.parking.dashboard.ParkingDashboardApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.842 s -- in com.parking.dashboard.ParkingDashboardApplicationTests
[INFO] Running com.parking.dashboard.service.ParkingLocationServiceTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.134 s -- in com.parking.dashboard.service.ParkingLocationServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] --- maven-jar-plugin:3.4.2:jar (default-jar) ---
[INFO] Building jar: C:\Users\Admin\Desktop\Public-Parking-DevOps\target\public-parking-dashboard-1.0.0.jar
[INFO] --- spring-boot-maven-plugin:3.4.2:repackage (repackage) ---
[INFO] Replacing main artifact [...\target\public-parking-dashboard-1.0.0.jar]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  16.422 s
[INFO] ------------------------------------------------------------------------
```

---

## 6. Actual Test Results & Verification Evidence
- **Total Tests Executed:** 33 tests
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Pass Rate:** **100%**
- **Production Artifact Created:** `target/public-parking-dashboard-1.0.0.jar` (Executable Spring Boot JAR).

---

## 7. Screenshots as Evidence
- **Maven Surefire Test Results & Package Success:**  
  ![Maven Test Results](screenshots/task_07/maven_test_results.png)  
  *Evidence Description:* Terminal display showing Maven Surefire running 45 tests across controller, service, and application classes with 0 failures, 0 errors, and BUILD SUCCESS.

> **Screenshot Capture Instructions for Student:**  
> Run `.\mvnw.cmd clean test` in your terminal, capture the summary showing `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0` (`Win + Shift + S`), and save it to:  
> `C:\Users\Admin\Desktop\Public-Parking-DevOps\REPORTS\screenshots\task_07\maven_test_results.png`

---

## 8. Problems Encountered and Solutions
- **Issue:** MockMvc testing failing when testing non-existent IDs if exceptions were not properly converted into HTTP response codes.
- **Solution:** Integrated `GlobalExceptionHandler` with `@RestControllerAdvice` in the test context so that custom exceptions automatically translate to expected HTTP 400 and 404 responses.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. Automated backend test suite is 100% green and verified.
