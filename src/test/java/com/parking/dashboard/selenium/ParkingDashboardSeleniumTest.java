package com.parking.dashboard.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium WebDriver E2E tests for Public Parking Dashboard.
 *
 * Prerequisites: Application must be running at APP_BASE_URL (default: http://localhost:8080)
 *
 * Run with: mvn test -Dtest=ParkingDashboardSeleniumTest -Dapp.base.url=http://localhost:8080
 * Run headless: mvn test -Dtest=ParkingDashboardSeleniumTest -Dselenium.headless=true
 *
 * These tests are in the 'selenium' package and excluded from the default Surefire run.
 * They are executed by the Maven Failsafe plugin or when explicitly triggered.
 *
 * Student: Sajiya Shaikh | Roll: 24102A2005
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Parking Dashboard Selenium E2E Tests")
class ParkingDashboardSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static String baseUrl;
    private static Path screenshotDir;

    @BeforeAll
    static void setupDriver() throws IOException {
        baseUrl = System.getProperty("app.base.url", "http://localhost:8080");
        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        String browser = System.getProperty("selenium.browser", "edge").toLowerCase();

        // Create screenshot directory
        screenshotDir = Paths.get("target", "selenium-screenshots");
        Files.createDirectories(screenshotDir);

        try {
            if ("firefox".equals(browser)) {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("--headless");
                driver = new FirefoxDriver(options);
            } else if ("chrome".equals(browser)) {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new", "--no-sandbox",
                                         "--disable-dev-shm-usage", "--window-size=1280,900");
                }
                driver = new ChromeDriver(options);
            } else {
                EdgeOptions options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new", "--no-sandbox",
                                         "--disable-dev-shm-usage", "--window-size=1280,900");
                }
                driver = new EdgeDriver(options);
            }

            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        } catch (Exception e) {
            System.err.println("[WARN] Selenium WebDriver could not be initialized: " + e.getMessage());
            Assumptions.assumeTrue(false, "Skipping Selenium browser tests: WebDriver binary unavailable (" + e.getMessage() + ")");
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterEach
    void onTestFailure(TestInfo testInfo) {
        // Screenshot on any uncaught exception is handled by @Test catch blocks
    }

    // ── Test 1: Dashboard loads and shows summary indicators ─────────────────

    @Test
    @Order(1)
    @DisplayName("T1: Dashboard loads and displays summary indicator cards")
    void test01_DashboardLoadsWithSummaryCards() {
        try {
            driver.get(baseUrl + "/");

            // Verify page title
            assertThat(driver.getTitle()).contains("Parking Dashboard");

            // Verify key summary cards are present
            WebElement totalLocationsCard = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("card-total-locations")));
            assertThat(totalLocationsCard.isDisplayed()).isTrue();

            WebElement availableCard = driver.findElement(By.id("card-available"));
            assertThat(availableCard.isDisplayed()).isTrue();

            WebElement occupiedCard = driver.findElement(By.id("card-occupied"));
            assertThat(occupiedCard.isDisplayed()).isTrue();

            WebElement utilizationCard = driver.findElement(By.id("card-utilization"));
            assertThat(utilizationCard.isDisplayed()).isTrue();

            // Verify the locations table is present
            WebElement table = driver.findElement(By.id("locations-table"));
            assertThat(table.isDisplayed()).isTrue();

            // Verify at least one row of sample data
            java.util.List<WebElement> rows = table.findElements(By.tagName("tr"));
            assertThat(rows.size()).isGreaterThan(1); // header + at least 1 data row

            System.out.println("[T1 PASS] Dashboard loaded successfully. " +
                               "Rows found: " + (rows.size() - 1));
        } catch (Exception e) {
            captureScreenshot("T1_dashboard_failure");
            throw e;
        }
    }

    // ── Test 2: Search for a parking location ────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("T2: Search for a parking location by name")
    void test02_SearchForParkingLocation() {
        try {
            driver.get(baseUrl + "/");

            // Find and use the search box
            WebElement searchBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("search")));
            searchBox.clear();
            searchBox.sendKeys("Bandra");

            // Submit search
            WebElement searchBtn = driver.findElement(By.id("btn-search"));
            searchBtn.click();

            // Wait for results
            wait.until(ExpectedConditions.urlContains("search=Bandra"));

            // Either results table or empty state should be visible
            boolean hasTable = !driver.findElements(By.id("locations-table")).isEmpty();
            boolean hasEmptyState = !driver.findElements(By.id("empty-state")).isEmpty();
            assertThat(hasTable || hasEmptyState).isTrue();

            if (hasTable) {
                WebElement table = driver.findElement(By.id("locations-table"));
                java.util.List<WebElement> rows = table.findElements(
                    By.cssSelector("tbody tr"));
                assertThat(rows.size()).isGreaterThan(0);
                // At least one row should contain 'Bandra' in the name
                boolean found = rows.stream()
                    .anyMatch(r -> r.getText().toLowerCase().contains("bandra"));
                assertThat(found).as("Expected to find 'Bandra' in search results").isTrue();
            }

            System.out.println("[T2 PASS] Search for 'Bandra' executed successfully.");
        } catch (Exception e) {
            captureScreenshot("T2_search_failure");
            throw e;
        }
    }

    // ── Test 3: Filter by availability status ────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("T3: Filter parking locations by FULL status")
    void test03_FilterByFullStatus() {
        try {
            driver.get(baseUrl + "/");

            // Click the 'Full' filter button
            WebElement filterFull = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("filter-full")));
            filterFull.click();

            // Wait for page to reflect filter
            wait.until(ExpectedConditions.urlContains("filter=FULL"));

            // All visible status badges should show FULL
            boolean hasTable = !driver.findElements(By.id("locations-table")).isEmpty();
            boolean hasEmptyState = !driver.findElements(By.id("empty-state")).isEmpty();
            assertThat(hasTable || hasEmptyState).isTrue();

            if (hasTable) {
                java.util.List<WebElement> statusBadges = driver.findElements(
                    By.cssSelector("#locations-table tbody .badge"));
                assertThat(statusBadges).isNotEmpty();
                statusBadges.forEach(badge ->
                    assertThat(badge.getText()).isEqualTo("FULL")
                );
            }

            System.out.println("[T3 PASS] Filter by FULL status works correctly.");
        } catch (Exception e) {
            captureScreenshot("T3_filter_failure");
            throw e;
        }
    }

    // ── Test 4: Open a parking location detail page ───────────────────────────

    @Test
    @Order(4)
    @DisplayName("T4: Open a parking location and verify its detail page")
    void test04_OpenParkingLocationDetail() {
        try {
            driver.get(baseUrl + "/");

            // Remove any filters first
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("locations-table")));

            // Click the first 'View Details' (eye icon) button
            WebElement viewBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#locations-table tbody tr:first-child .btn-outline-primary")));
            viewBtn.click();

            // Wait for detail page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("location-detail-card")));

            // Verify detail card elements
            WebElement statusBadge = driver.findElement(By.id("location-status-badge"));
            assertThat(statusBadge.isDisplayed()).isTrue();

            WebElement utilizationBar = driver.findElement(By.id("utilization-bar"));
            assertThat(utilizationBar.isDisplayed()).isTrue();

            WebElement locCapacity = driver.findElement(By.id("loc-capacity"));
            assertThat(locCapacity.getText()).matches("\\d+");

            WebElement locOccupied = driver.findElement(By.id("loc-occupied"));
            assertThat(locOccupied.getText()).matches("\\d+");

            WebElement locAvailable = driver.findElement(By.id("loc-available"));
            assertThat(locAvailable.getText()).matches("\\d+");

            System.out.println("[T4 PASS] Location detail page loaded. " +
                               "Status: " + statusBadge.getText() +
                               ", Capacity: " + locCapacity.getText());
        } catch (Exception e) {
            captureScreenshot("T4_detail_failure");
            throw e;
        }
    }

    // ── Test 5: Submit a valid parking update ─────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("T5: Submit a valid parking occupancy update")
    void test05_SubmitValidParkingUpdate() {
        try {
            driver.get(baseUrl + "/events/new");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("event-form")));

            // Select the first available location
            Select locationSelect = new Select(
                driver.findElement(By.id("parkingLocationId")));
            assertThat(locationSelect.getOptions().size()).isGreaterThan(1);
            locationSelect.selectByIndex(1); // Select first real location

            // Select event type: OCCUPANCY_UPDATE
            Select typeSelect = new Select(driver.findElement(By.id("eventType")));
            typeSelect.selectByValue("OCCUPANCY_UPDATE");

            // Enter a valid occupancy
            WebElement occupancyField = driver.findElement(By.id("reportedOccupancy"));
            occupancyField.clear();
            occupancyField.sendKeys("25");

            // Enter operator name
            WebElement operatorField = driver.findElement(By.id("operatorName"));
            operatorField.clear();
            operatorField.sendKeys("Selenium Test Operator");

            // Enter description
            WebElement descField = driver.findElement(By.id("description"));
            descField.clear();
            descField.sendKeys("Automated Selenium test update - " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Submit the form
            WebElement submitBtn = driver.findElement(By.id("btn-submit-event"));
            submitBtn.click();

            // After successful submission, should redirect to the location detail page
            wait.until(ExpectedConditions.urlContains("/locations/"));

            // Check for success message
            boolean hasSuccess = !driver.findElements(By.cssSelector(".alert-success")).isEmpty();
            assertThat(hasSuccess).isTrue();

            System.out.println("[T5 PASS] Valid parking update submitted successfully.");
        } catch (Exception e) {
            captureScreenshot("T5_update_failure");
            throw e;
        }
    }

    // ── Test 6 (Recommended): Submit invalid update and verify validation ──────

    @Test
    @Order(6)
    @DisplayName("T6: Submit invalid parking update (negative occupancy) and verify validation")
    void test06_SubmitInvalidUpdateShowsValidation() {
        try {
            driver.get(baseUrl + "/events/new");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("event-form")));

            // Select location
            Select locationSelect = new Select(driver.findElement(By.id("parkingLocationId")));
            if (locationSelect.getOptions().size() > 1) {
                locationSelect.selectByIndex(1);
            }

            // Select event type
            Select typeSelect = new Select(driver.findElement(By.id("eventType")));
            typeSelect.selectByValue("OCCUPANCY_UPDATE");

            // Enter invalid negative occupancy
            WebElement occupancyField = driver.findElement(By.id("reportedOccupancy"));
            occupancyField.clear();
            occupancyField.sendKeys("-5");

            // Try to submit
            WebElement submitBtn = driver.findElement(By.id("btn-submit-event"));
            submitBtn.click();

            // Should NOT redirect to success page - should stay on form or show error
            // HTML5 validation may prevent form submission
            // If server-side validation fires: check for error message
            boolean onFormPage = driver.getCurrentUrl().contains("/events") ||
                                  driver.getCurrentUrl().contains("/new");
            boolean hasError = !driver.findElements(By.cssSelector(".alert-danger, .invalid-feedback")).isEmpty();

            // Either HTML5 validation blocked it (stays on form page) or server returned error
            assertThat(onFormPage || hasError).isTrue();

            System.out.println("[T6 PASS] Invalid occupancy (-5) correctly rejected. " +
                               "On form: " + onFormPage + ", Has error: " + hasError);
        } catch (Exception e) {
            captureScreenshot("T6_validation_failure");
            throw e;
        }
    }

    // ── Test 7 (Extra): Alerts page loads ─────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("T7: Alerts page loads and displays alert counts")
    void test07_AlertsPageLoads() {
        try {
            driver.get(baseUrl + "/alerts");

            // Verify alert count cards
            WebElement criticalCard = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("alert-count-critical")));
            assertThat(criticalCard.isDisplayed()).isTrue();

            WebElement highCard = driver.findElement(By.id("alert-count-high"));
            assertThat(highCard.isDisplayed()).isTrue();

            // Either alerts list or "All Clear" message should be visible
            boolean hasAlertsList = !driver.findElements(By.id("alerts-list")).isEmpty();
            boolean hasClearMessage = !driver.findElements(By.cssSelector(".bi-shield-check")).isEmpty();
            assertThat(hasAlertsList || hasClearMessage).isTrue();

            System.out.println("[T7 PASS] Alerts page loaded. " +
                               "Critical count: " + criticalCard.findElement(By.className("display-6")).getText());
        } catch (Exception e) {
            captureScreenshot("T7_alerts_failure");
            throw e;
        }
    }

    // ── Screenshot utility ────────────────────────────────────────────────────

    private void captureScreenshot(String testName) {
        try {
            if (driver instanceof TakesScreenshot) {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path dest = screenshotDir.resolve(testName + "_" + timestamp + ".png");
                Files.copy(src.toPath(), dest);
                System.out.println("[SCREENSHOT] Saved: " + dest.toAbsolutePath());
            }
        } catch (IOException ex) {
            System.err.println("Failed to save screenshot: " + ex.getMessage());
        }
    }
}
