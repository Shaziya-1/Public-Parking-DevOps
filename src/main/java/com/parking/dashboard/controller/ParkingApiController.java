package com.parking.dashboard.controller;

import com.parking.dashboard.dto.DashboardSummaryDto;
import com.parking.dashboard.dto.ParkingAlertDto;
import com.parking.dashboard.dto.ParkingEventFormDto;
import com.parking.dashboard.model.ParkingEvent;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.service.ParkingEventService;
import com.parking.dashboard.service.ParkingLocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for the Public Parking Dashboard.
 * All endpoints return JSON. Base path: /api/v1
 *
 * API Documentation available in docs/api-specification.md
 */
@RestController
@RequestMapping("/api/v1")
public class ParkingApiController {

    private final ParkingLocationService locationService;
    private final ParkingEventService eventService;

    public ParkingApiController(ParkingLocationService locationService,
                                 ParkingEventService eventService) {
        this.locationService = locationService;
        this.eventService = eventService;
    }

    // ── Health Check ──────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Public Parking Dashboard",
            "version", "1.0.0"
        ));
    }

    // ── Dashboard Summary ─────────────────────────────────────────────────────

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(locationService.getDashboardSummary());
    }

    // ── Parking Locations ─────────────────────────────────────────────────────

    @GetMapping("/locations")
    public ResponseEntity<List<ParkingLocation>> getAllLocations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter) {
        List<ParkingLocation> result;
        if (search != null && !search.isBlank()) {
            result = locationService.search(search);
        } else if (filter != null && !filter.isBlank()) {
            result = locationService.filterByStatus(filter);
        } else {
            result = locationService.findAll();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<ParkingLocation> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    @PostMapping("/locations")
    public ResponseEntity<ParkingLocation> createLocation(
            @Valid @RequestBody ParkingLocation location) {
        ParkingLocation saved = locationService.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<ParkingLocation> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLocation location) {
        return ResponseEntity.ok(locationService.update(id, location));
    }

    @PatchMapping("/locations/{id}/occupancy")
    public ResponseEntity<ParkingLocation> updateOccupancy(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer newOccupancy = body.get("occupiedSpaces");
        if (newOccupancy == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(locationService.updateOccupancy(id, newOccupancy));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Parking Events ────────────────────────────────────────────────────────

    @PostMapping("/events")
    public ResponseEntity<ParkingEvent> recordEvent(
            @Valid @RequestBody ParkingEventFormDto formDto) {
        ParkingEvent saved = eventService.recordEvent(formDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/events/location/{locationId}")
    public ResponseEntity<List<ParkingEvent>> getEventsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(eventService.findEventsForLocation(locationId));
    }

    @GetMapping("/events/recent")
    public ResponseEntity<List<ParkingEvent>> getRecentEvents(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(eventService.findRecentEvents(Math.min(limit, 100)));
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    @GetMapping("/alerts")
    public ResponseEntity<List<ParkingAlertDto>> getAlerts() {
        return ResponseEntity.ok(locationService.getAlerts());
    }
}
