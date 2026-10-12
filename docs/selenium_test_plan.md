# Selenium WebDriver End-to-End (E2E) Test Plan

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  

---

## 1. Test Strategy & Objectives

The Selenium test suite verifies that end-users, drivers, and parking lot operators experience seamless and bug-free interactions on the web front-end. The suite operates headlessly in continuous integration pipelines and generates diagnostic failure screenshots when assertions fail.

---

## 2. Test Case Matrix

| ID | Test Name | Target UI Feature | User Flow / Verification |
| :--- | :--- | :--- | :--- |
| **T1** | `test01_DashboardLoadsWithSummaryCards` | Home Dashboard (`/`) | Navigates to `/`. Verifies page title, total locations card, available spots card, occupied card, utilization card, and table row count > 1. |
| **T2** | `test02_SearchForParkingLocation` | Search Box (`#search`) | Enters search term "Bandra". Clicks `#btn-search`. Asserts URL parameters update and filtered rows contain "Bandra". |
| **T3** | `test03_FilterByFullStatus` | Status Filter (`#filter-full`) | Clicks "Full" filter. Asserts all visible rows exhibit the `FULL` badge. |
| **T4** | `test04_OpenParkingLocationDetail` | Location Details (`/locations/{id}`) | Clicks "View Details" on first table row. Asserts detail card renders capacity, occupied, available, and progress bar. |
| **T5** | `test05_SubmitValidParkingUpdate` | Event Entry Form (`/events/new`) | Selects location, event type `OCCUPANCY_UPDATE`, enters valid occupancy (25), operator name, and submits. Asserts redirect and success banner. |
| **T6** | `test06_SubmitInvalidUpdateShowsValidation` | Boundary Validation | Enters negative occupancy (-5). Asserts client HTML5 validation blocks submission or server rejects with validation error. |
| **T7** | `test07_AlertsPageLoads` | Alerts View (`/alerts`) | Loads `/alerts`. Verifies critical and high alert cards, saturation badges, and active alert list. |

---

## 3. Cross-Browser & Headless Configuration

The test suite supports dynamic browser selection via JVM system properties:

```bash
# Microsoft Edge (Default on Windows host)
mvn test "-Dtest=ParkingDashboardSeleniumTest" "-Dselenium.browser=edge" "-Dselenium.headless=true"

# Google Chrome (CI / Linux agent)
mvn test "-Dtest=ParkingDashboardSeleniumTest" "-Dselenium.browser=chrome" "-Dselenium.headless=true"

# Mozilla Firefox
mvn test "-Dtest=ParkingDashboardSeleniumTest" "-Dselenium.browser=firefox" "-Dselenium.headless=true"
```

### Headless Arguments Applied:
- `--headless=new`: Modern Chromium headless rendering engine.
- `--no-sandbox`: Avoids sandboxing issues in containerized CI agents.
- `--disable-dev-shm-usage`: Uses `/tmp` instead of shared memory for high-volume DOM parsing.
- `--window-size=1280,900`: Standard desktop viewport resolution.

---

## 4. Failure Screenshot Capture

Whenever an assertion or unhandled exception occurs during a test:
1. `captureScreenshot(testName)` triggers via `TakesScreenshot`.
2. Image is saved to: `target/selenium-screenshots/<testName>_<timestamp>.png`.
3. Stack trace and screenshot location are logged to the console and Surefire XML report.

---

## 5. Execution Results & Environment Diagnostics

- **Test Class:** `com.parking.dashboard.selenium.ParkingDashboardSeleniumTest`
- **Surefire XML Output:** `target/surefire-reports/TEST-com.parking.dashboard.selenium.ParkingDashboardSeleniumTest.xml`
- **Driver Management:** Selenium 4.21.0 native Selenium Manager with fallback handling for restricted network environments.
