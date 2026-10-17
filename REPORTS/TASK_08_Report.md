# Task 08 Report: Browser Automation Suite (Selenium WebDriver)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh  
**Roll Number:** 24102A2005  
**Milestone:** Task 08  
**Status:** **COMPLETED**  

---

## 1. Objective
Author and execute an end-to-end (E2E) automated browser test suite using Selenium WebDriver to simulate real user interactions across the web dashboard, validating form submissions, search filtering, capacity checks, and live telemetry updates in a real browser.

---

## 2. Tools and Technologies Used
- **Automation Framework:** Selenium WebDriver 4.21.0 (`selenium-java`)
- **Driver Management:** Local 64-bit Edge WebDriver binary (`drivers/msedgedriver.exe`, version `155.0.4283.45`)
- **Test Runner:** JUnit 5 (JUnit Platform Provider via Maven Surefire 3.2.5)
- **Target Browser:** Microsoft Edge (Chromium engine version `155.0.4283.45`) in headless mode with 1920x1080 resolution
- **Application Target:** Reverse Proxy endpoint `http://localhost:80` (backed by Docker Compose network)

---

## 3. Work Performed
1. Downloaded and configured the exact matching 64-bit Microsoft Edge WebDriver binary (`155.0.4283.45`) inside `drivers/msedgedriver.exe`.
2. Updated `ParkingDashboardSeleniumTest.java` to:
   - Target the live Nginx reverse proxy endpoint `http://localhost:80`.
   - Set browser window dimensions to `1920,1080` to ensure full desktop responsive layout.
   - Implement robust JavaScript scrolling and explicit clickable condition helpers (`clickElement`).
   - Automatically capture timestamped PNG screenshots on both test success and test failure into `target/selenium-screenshots/`.
3. Executed 7 comprehensive End-to-End browser test scenarios:
   - `testDashboardLoadsAndShowsTable()`: Validates home page load, summary cards, and verifies 24 real BMC parking locations rendered.
   - `testSearchFilter()`: Types "Bandra" into the live search filter input and validates filtered row count.
   - `testStatusFilter()`: Filters table by status "FULL" and verifies matching rows.
   - `testLocationDetailNavigation()`: Clicks detail link for facility and verifies capacity (300) and status attributes.
   - `testUpdateOccupancyFlow()`: Submits occupancy update via form and validates updated count.
   - `testValidationRejection()`: Submits negative occupancy value (-5) and validates HTML5/backend validation rejection.
   - `testAlertsPage()`: Navigates to `/alerts` and validates alert table and stale data indicators.
4. Exported all generated screenshots into `REPORTS/screenshots/task_08/`.

---

## 4. Files Created or Modified
- `drivers/msedgedriver.exe`: 64-bit Microsoft Edge WebDriver binary.
- `src/test/java/com/parking/dashboard/selenium/ParkingDashboardSeleniumTest.java`: 7 Selenium E2E test scenarios with automated screenshot capturing.
- `docs/selenium_test_plan.md`: Comprehensive test plan, locator strategy, and test case matrices.
- `REPORTS/screenshots/task_08/*.png`: 7 verified PNG screenshots captured during test execution.

---

## 5. Commands Executed
```powershell
# Run the complete Selenium E2E suite against Nginx reverse proxy on port 80
.\mvnw.cmd test "-Dtest=ParkingDashboardSeleniumTest" "-Dapp.base.url=http://localhost:80"

# Copy verified screenshots to task reports directory
Copy-Item -Path 'target\selenium-screenshots\*.png' -Destination 'REPORTS\screenshots\task_08\' -Force
```

### Execution Log / Terminal Output
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.parking.dashboard.selenium.ParkingDashboardSeleniumTest
[SCREENSHOT] Saved: target\selenium-screenshots\T1_dashboard_success_20261017_210617.png
[T1 PASS] Dashboard loaded successfully. Rows found: 24
[SCREENSHOT] Saved: target\selenium-screenshots\T2_search_success_20261017_210623.png
[T2 PASS] Search for 'Bandra' executed successfully.
[SCREENSHOT] Saved: target\selenium-screenshots\T3_filter_success_20261017_210630.png
[T3 PASS] Filter by FULL status works correctly.
[SCREENSHOT] Saved: target\selenium-screenshots\T4_detail_success_20261017_210630.png
[T4 PASS] Location detail page loaded. Status: HIGH, Capacity: 300
[SCREENSHOT] Saved: target\selenium-screenshots\T5_update_success_20261017_210632.png
[T5 PASS] Valid parking update submitted successfully.
[SCREENSHOT] Saved: target\selenium-screenshots\T6_validation_success_20261017_210633.png
[T6 PASS] Invalid occupancy (-5) correctly rejected.
[SCREENSHOT] Saved: target\selenium-screenshots\T7_alerts_success_20261017_210639.png
[T7 PASS] Alerts page loaded successfully.
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 26.78 s -- in com.parking.dashboard.selenium.ParkingDashboardSeleniumTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Actual Test Results & Verification Evidence
- **Total Test Cases Executed:** 7
- **Passed:** 7 (100%)
- **Failed:** 0
- **Errors:** 0
- **Skipped:** 0
- **Execution Time:** 26.78 seconds
- **Browser Executed:** Microsoft Edge 155.0.4283.45 (Headless, 1920x1080)
- **Target URL:** `http://localhost:80` (Nginx reverse proxy routing to Spring Boot container)

---

## 7. Screenshots as Evidence
- **T1 — Dashboard Table Load (24 Real BMC Facilities):**  
  ![T1 Dashboard Success](screenshots/task_08/T1_dashboard_success_20261017_210617.png)  
  *Evidence Description:* Automated screenshot of dashboard view rendering all 24 verified BMC Mumbai parking facilities, summary cards, and search filters.

- **T2 — Search Filter ('Bandra'):**  
  ![T2 Search Success](screenshots/task_08/T2_search_success_20261017_210623.png)  
  *Evidence Description:* Automated screenshot showing real-time text filter filtering table rows to Bandra facilities.

- **T3 — Status Filter ('FULL'):**  
  ![T3 Filter Success](screenshots/task_08/T3_filter_success_20261017_210630.png)  
  *Evidence Description:* Automated screenshot showing status dropdown filter isolating facilities currently at full capacity.

- **T4 — Facility Detail View:**  
  ![T4 Detail Success](screenshots/task_08/T4_detail_success_20261017_210630.png)  
  *Evidence Description:* Automated screenshot showing facility details, capacity, and live status.

- **T5 — Update Occupancy Flow:**  
  ![T5 Update Success](screenshots/task_08/T5_update_success_20261017_210632.png)  
  *Evidence Description:* Automated screenshot showing successful occupancy update submission.

- **T6 — Input Validation Rejection:**  
  ![T6 Validation Success](screenshots/task_08/T6_validation_success_20261017_210633.png)  
  *Evidence Description:* Automated screenshot demonstrating validation error when submitting negative values.

- **T7 — Alerts & Stale Data Detection:**  
  ![T7 Alerts Success](screenshots/task_08/T7_alerts_success_20261017_210639.png)  
  *Evidence Description:* Automated screenshot showing alerts dashboard with facilities flagged for stale telemetry or high occupancy.

---

## 8. Problems Encountered and Solutions
1. **CDP Version Warning:** Selenium 4.21.0 reported missing CDP implementation for Chromium 155. Handled by disabling unnecessary CDP network inspection listeners and relying on standard W3C WebDriver actions.
2. **Search Input Element Obscured:** Search input was partially obscured on smaller default viewports. Resolved by explicitly setting Edge window size to 1920x1080 and adding scrolling helpers.
3. **Driver Availability:** Downloaded official Microsoft Edge WebDriver binary matching build 155.0.4283.45 to `drivers/msedgedriver.exe` and configured `webdriver.edge.driver` system property.

---

## 9. Final Status & Remaining Work
- **Status:** **COMPLETED**
- **Remaining Work:** None. All 7 scenarios execute and pass cleanly against the live containerized stack.
