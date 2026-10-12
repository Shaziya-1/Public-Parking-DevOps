package com.parking.dashboard.service;

import com.parking.dashboard.config.ParkingConfig;
import com.parking.dashboard.dto.DashboardSummaryDto;
import com.parking.dashboard.dto.ParkingAlertDto;
import com.parking.dashboard.exception.InvalidParkingUpdateException;
import com.parking.dashboard.exception.ParkingLocationNotFoundException;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ParkingLocationService.
 * Uses the H2 test database profile.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ParkingLocationService Integration Tests")
class ParkingLocationServiceTest {

    @Autowired
    private ParkingLocationService locationService;

    @Autowired
    private ParkingLocationRepository locationRepo;

    @Autowired
    private ParkingConfig config;

    private ParkingLocation savedLocation;

    @BeforeEach
    void setUp() {
        // Create a test parking location in H2 test database
        ParkingLocation loc = new ParkingLocation(
                "Test Parking - Unit Test Location",
                "Test Area",
                "123 Test Street, Test City",
                100, 40);
        savedLocation = locationService.save(loc);
    }

    // ── CRUD Tests ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should save a new parking location")
    void shouldSaveNewLocation() {
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getName()).isEqualTo("Test Parking - Unit Test Location");
        assertThat(savedLocation.getTotalCapacity()).isEqualTo(100);
        assertThat(savedLocation.getOccupiedSpaces()).isEqualTo(40);
    }

    @Test
    @DisplayName("Should retrieve a location by ID")
    void shouldFindById() {
        ParkingLocation found = locationService.findById(savedLocation.getId());
        assertThat(found.getId()).isEqualTo(savedLocation.getId());
        assertThat(found.getName()).isEqualTo("Test Parking - Unit Test Location");
    }

    @Test
    @DisplayName("Should throw ParkingLocationNotFoundException for unknown ID")
    void shouldThrowNotFoundForUnknownId() {
        assertThatThrownBy(() -> locationService.findById(99999L))
                .isInstanceOf(ParkingLocationNotFoundException.class);
    }

    @Test
    @DisplayName("Should update a parking location")
    void shouldUpdateLocation() {
        ParkingLocation update = new ParkingLocation(
                "Updated Name", "Updated Area", "Updated Address", 150, 75);
        ParkingLocation updated = locationService.update(savedLocation.getId(), update);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getTotalCapacity()).isEqualTo(150);
        assertThat(updated.getOccupiedSpaces()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should soft-delete a parking location")
    void shouldSoftDeleteLocation() {
        Long id = savedLocation.getId();
        locationService.delete(id);

        assertThatThrownBy(() -> locationService.findById(id))
                .isInstanceOf(ParkingLocationNotFoundException.class);
    }

    // ── Availability Calculation Tests ────────────────────────────────────────

    @Test
    @DisplayName("Should calculate available spaces correctly")
    void shouldCalculateAvailableSpaces() {
        // capacity=100, occupied=40, available=60
        assertThat(savedLocation.getAvailableSpaces()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should calculate utilization percentage correctly")
    void shouldCalculateUtilizationPercentage() {
        // 40/100 = 40%
        assertThat(savedLocation.getUtilizationPercentage()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Should identify a full parking location")
    void shouldIdentifyFullLocation() {
        ParkingLocation full = new ParkingLocation("Full Lot", "Area", "Address", 50, 50);
        ParkingLocation saved = locationService.save(full);
        assertThat(saved.isFull()).isTrue();
        assertThat(saved.getAvailableSpaces()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return HIGH status for low-occupancy location")
    void shouldReturnHighStatusForLowOccupancy() {
        // 40% utilization → HIGH availability
        assertThat(savedLocation.getAvailabilityStatus()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Should return FULL status for full location")
    void shouldReturnFullStatusForFullLocation() {
        ParkingLocation full = locationService.save(
                new ParkingLocation("Full Lot", "X", "Y", 50, 50));
        assertThat(full.getAvailabilityStatus()).isEqualTo("FULL");
    }

    // ── Validation Tests ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should reject occupancy exceeding capacity")
    void shouldRejectOccupancyExceedingCapacity() {
        ParkingLocation invalid = new ParkingLocation("Bad Lot", "Area", "Addr", 50, 99);
        assertThatThrownBy(() -> locationService.save(invalid))
                .isInstanceOf(InvalidParkingUpdateException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("Should reject negative occupancy")
    void shouldRejectNegativeOccupancy() {
        ParkingLocation invalid = new ParkingLocation("Bad Lot", "Area", "Addr", 50, -5);
        assertThatThrownBy(() -> locationService.save(invalid))
                .isInstanceOf(InvalidParkingUpdateException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should accept zero occupancy")
    void shouldAcceptZeroOccupancy() {
        ParkingLocation empty = new ParkingLocation("Empty Lot", "Area", "Addr", 50, 0);
        ParkingLocation saved = locationService.save(empty);
        assertThat(saved.getOccupiedSpaces()).isEqualTo(0);
        assertThat(saved.getAvailableSpaces()).isEqualTo(50);
    }

    // ── Search and Filter Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("Should search locations by name")
    void shouldSearchByName() {
        List<ParkingLocation> results = locationService.search("Unit Test Location");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).contains("Unit Test Location");
    }

    @Test
    @DisplayName("Should search locations by area")
    void shouldSearchByArea() {
        List<ParkingLocation> results = locationService.search("Test Area");
        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should return empty list for non-matching search")
    void shouldReturnEmptyForNonMatchingSearch() {
        List<ParkingLocation> results = locationService.search("zzz_no_match_xyz");
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should filter by FULL status")
    void shouldFilterFullLocations() {
        ParkingLocation full = locationService.save(
                new ParkingLocation("Full Filter Test", "Area", "Addr", 30, 30));
        List<ParkingLocation> fullLocations = locationService.filterByStatus("FULL");
        assertThat(fullLocations).anyMatch(l -> l.getId().equals(full.getId()));
    }

    // ── Dashboard Summary Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("Should compute dashboard summary with correct totals")
    void shouldComputeDashboardSummary() {
        DashboardSummaryDto summary = locationService.getDashboardSummary();

        assertThat(summary.getTotalLocations()).isGreaterThan(0);
        assertThat(summary.getTotalCapacity()).isGreaterThan(0);
        assertThat(summary.getTotalOccupied()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getTotalAvailable()).isGreaterThanOrEqualTo(0);
        // Verify: capacity = occupied + available
        assertThat(summary.getTotalCapacity())
                .isEqualTo(summary.getTotalOccupied() + summary.getTotalAvailable());
    }

    @Test
    @DisplayName("Should generate alerts for full locations")
    void shouldGenerateAlertsForFullLocations() {
        // Add a full location to trigger alert
        locationService.save(new ParkingLocation("Alert Full Lot", "AlertArea", "Addr", 20, 20));
        List<ParkingAlertDto> alerts = locationService.getAlerts();

        boolean hasFull = alerts.stream()
                .anyMatch(a -> a.getAlertType() == ParkingAlertDto.AlertType.FULL);
        assertThat(hasFull).isTrue();
    }

    // ── Occupancy Update Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("Should update occupancy successfully")
    void shouldUpdateOccupancySuccessfully() {
        ParkingLocation updated = locationService.updateOccupancy(savedLocation.getId(), 70);
        assertThat(updated.getOccupiedSpaces()).isEqualTo(70);
        assertThat(updated.getAvailableSpaces()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should reject occupancy update exceeding capacity")
    void shouldRejectOccupancyUpdateExceedingCapacity() {
        // savedLocation has capacity=100
        assertThatThrownBy(() -> locationService.updateOccupancy(savedLocation.getId(), 150))
                .isInstanceOf(InvalidParkingUpdateException.class);
    }
}
