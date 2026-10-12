package com.parking.dashboard.service;

import com.parking.dashboard.config.ParkingConfig;
import com.parking.dashboard.dto.DashboardSummaryDto;
import com.parking.dashboard.dto.ParkingAlertDto;
import com.parking.dashboard.exception.InvalidParkingUpdateException;
import com.parking.dashboard.exception.ParkingLocationNotFoundException;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for parking location business logic.
 */
@Service
@Transactional
public class ParkingLocationService {

    private static final Logger log = LoggerFactory.getLogger(ParkingLocationService.class);

    private final ParkingLocationRepository locationRepo;
    private final ParkingConfig config;

    public ParkingLocationService(ParkingLocationRepository locationRepo, ParkingConfig config) {
        this.locationRepo = locationRepo;
        this.config = config;
    }

    // ── Read Operations ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ParkingLocation> findAll() {
        return locationRepo.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public ParkingLocation findById(Long id) {
        return locationRepo.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ParkingLocationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ParkingLocation> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        return locationRepo.searchLocations(query.trim());
    }

    @Transactional(readOnly = true)
    public List<ParkingLocation> filterByStatus(String status) {
        return switch (status.toUpperCase()) {
            case "FULL"     -> locationRepo.findFullLocations();
            case "LOW"      -> locationRepo.findLowAvailabilityLocations(config.getLowAvailabilityThreshold());
            case "STALE"    -> locationRepo.findStaleLocations(
                                  LocalDateTime.now().minusMinutes(config.getStaleThresholdMinutes()));
            case "AVAILABLE"-> {
                // Available = has free spaces and NOT low
                List<ParkingLocation> all = findAll();
                yield all.stream()
                         .filter(p -> p.getAvailableSpaces() > 0)
                         .filter(p -> p.getUtilizationPercentage() < config.getLowAvailabilityThreshold())
                         .toList();
            }
            default -> findAll();
        };
    }

    // ── Write Operations ──────────────────────────────────────────────────────

    public ParkingLocation save(ParkingLocation location) {
        validateOccupancy(location.getOccupiedSpaces(), location.getTotalCapacity());
        if (location.getLastUpdated() == null) {
            location.setLastUpdated(LocalDateTime.now());
        }
        ParkingLocation saved = locationRepo.save(location);
        log.info("Saved parking location: {}", saved.getName());
        return saved;
    }

    public ParkingLocation update(Long id, ParkingLocation updated) {
        ParkingLocation existing = findById(id);
        validateOccupancy(updated.getOccupiedSpaces(), updated.getTotalCapacity());

        existing.setName(updated.getName());
        existing.setArea(updated.getArea());
        existing.setAddress(updated.getAddress());
        existing.setTotalCapacity(updated.getTotalCapacity());
        existing.setOccupiedSpaces(updated.getOccupiedSpaces());
        existing.setLocationCode(updated.getLocationCode());
        existing.setContactPhone(updated.getContactPhone());
        existing.setDescription(updated.getDescription());
        existing.setLastUpdated(LocalDateTime.now());

        ParkingLocation saved = locationRepo.save(existing);
        log.info("Updated parking location id={}", id);
        return saved;
    }

    public ParkingLocation updateOccupancy(Long id, Integer newOccupancy) {
        ParkingLocation location = findById(id);
        validateOccupancy(newOccupancy, location.getTotalCapacity());
        location.setOccupiedSpaces(newOccupancy);
        location.setLastUpdated(LocalDateTime.now());
        return locationRepo.save(location);
    }

    public void delete(Long id) {
        ParkingLocation loc = findById(id);
        loc.setActive(false);
        locationRepo.save(loc);
        log.info("Soft-deleted parking location id={}", id);
    }

    // ── Dashboard Summary ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        long total     = locationRepo.countByActiveTrue();
        long capacity  = locationRepo.sumTotalCapacity();
        long occupied  = locationRepo.sumOccupiedSpaces();
        long available = Math.max(0, capacity - occupied);

        double utilPct = (capacity > 0) ? ((double) occupied / capacity) * 100.0 : 0.0;

        List<ParkingLocation> fullList = locationRepo.findFullLocations();
        List<ParkingLocation> lowList  = locationRepo.findLowAvailabilityLocations(
                                             config.getLowAvailabilityThreshold());
        List<ParkingLocation> staleList = locationRepo.findStaleLocations(
                                             LocalDateTime.now().minusMinutes(config.getStaleThresholdMinutes()));

        summary.setTotalLocations(total);
        summary.setTotalCapacity(capacity);
        summary.setTotalOccupied(occupied);
        summary.setTotalAvailable(available);
        summary.setOverallUtilizationPercentage(Math.round(utilPct * 10.0) / 10.0);
        summary.setFullLocations(fullList.size());
        summary.setLowAvailabilityLocations(lowList.size());
        summary.setStaleLocations(staleList.size());
        summary.setLowAvailabilityThreshold(config.getLowAvailabilityThreshold());

        return summary;
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ParkingAlertDto> getAlerts() {
        List<ParkingAlertDto> alerts = new ArrayList<>();

        // Full locations
        locationRepo.findFullLocations().forEach(loc -> {
            ParkingAlertDto alert = new ParkingAlertDto();
            alert.setLocationId(loc.getId());
            alert.setLocationName(loc.getName());
            alert.setArea(loc.getArea());
            alert.setAlertType(ParkingAlertDto.AlertType.FULL);
            alert.setSeverity(ParkingAlertDto.Severity.CRITICAL);
            alert.setReason("Parking lot is full. No spaces available.");
            alert.setAvailableSpaces(loc.getAvailableSpaces());
            alert.setTotalCapacity(loc.getTotalCapacity());
            alert.setUtilizationPercentage(loc.getUtilizationPercentage());
            alerts.add(alert);
        });

        // Low availability
        locationRepo.findLowAvailabilityLocations(config.getLowAvailabilityThreshold()).forEach(loc -> {
            ParkingAlertDto alert = new ParkingAlertDto();
            alert.setLocationId(loc.getId());
            alert.setLocationName(loc.getName());
            alert.setArea(loc.getArea());
            alert.setAlertType(ParkingAlertDto.AlertType.LOW_AVAILABILITY);
            alert.setSeverity(ParkingAlertDto.Severity.HIGH);
            alert.setReason(String.format("Only %d spaces available (%.1f%% occupied).",
                loc.getAvailableSpaces(), loc.getUtilizationPercentage()));
            alert.setAvailableSpaces(loc.getAvailableSpaces());
            alert.setTotalCapacity(loc.getTotalCapacity());
            alert.setUtilizationPercentage(loc.getUtilizationPercentage());
            alerts.add(alert);
        });

        // Stale data
        locationRepo.findStaleLocations(
                LocalDateTime.now().minusMinutes(config.getStaleThresholdMinutes())).forEach(loc -> {
            ParkingAlertDto alert = new ParkingAlertDto();
            alert.setLocationId(loc.getId());
            alert.setLocationName(loc.getName());
            alert.setArea(loc.getArea());
            alert.setAlertType(ParkingAlertDto.AlertType.STALE_DATA);
            alert.setSeverity(ParkingAlertDto.Severity.MEDIUM);
            alert.setReason("Data has not been updated within the expected time window.");
            alert.setAvailableSpaces(loc.getAvailableSpaces());
            alert.setTotalCapacity(loc.getTotalCapacity());
            alert.setUtilizationPercentage(loc.getUtilizationPercentage());
            alerts.add(alert);
        });

        return alerts;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateOccupancy(Integer occupied, Integer capacity) {
        if (occupied == null || capacity == null) return;
        if (occupied < 0) {
            throw new InvalidParkingUpdateException(
                "Occupied spaces cannot be negative. Provided: " + occupied);
        }
        if (occupied > capacity) {
            throw new InvalidParkingUpdateException(
                "Occupied spaces (" + occupied + ") cannot exceed total capacity (" + capacity + ").");
        }
    }
}
