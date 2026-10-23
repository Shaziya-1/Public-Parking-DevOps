package com.parking.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dashboard.dto.ParkingEventFormDto;
import com.parking.dashboard.model.ParkingEvent;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.service.ParkingEventService;
import com.parking.dashboard.service.ParkingLocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API controller integration tests using MockMvc.
 * Tests all major API endpoints for correct HTTP status and response structure.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ParkingApiController Integration Tests")
class ParkingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingLocationService locationService;

    @Autowired
    private ParkingEventService eventService;

    // ── Health Check ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/health should return 200 with status UP")
    void healthEndpointReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").exists());
    }

    // ── Dashboard Summary ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/dashboard/summary should return summary DTO")
    void dashboardSummaryReturnsCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLocations").isNumber())
                .andExpect(jsonPath("$.totalCapacity").isNumber())
                .andExpect(jsonPath("$.totalOccupied").isNumber())
                .andExpect(jsonPath("$.totalAvailable").isNumber())
                .andExpect(jsonPath("$.overallUtilizationPercentage").isNumber());
    }

    // ── List Locations ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/locations should return list of locations")
    void getAllLocationsReturnsArray() throws Exception {
        // Seed test data
        locationService.save(new ParkingLocation("API Test Location", "TestArea", "Addr", 100, 30));

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/locations?search=API should return matching locations")
    void searchLocationsReturnsMatchingResults() throws Exception {
        locationService.save(new ParkingLocation("API Search Test Lot", "SearchArea", "Addr", 80, 10));

        mockMvc.perform(get("/api/v1/locations").param("search", "API Search Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value(containsString("API Search Test")));
    }

    // ── Create Location ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/locations should create a new location and return 201")
    void createLocationReturns201() throws Exception {
        ParkingLocation newLoc = new ParkingLocation(
                "New API Location", "Whitefield", "100 Test Road", 200, 50);

        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New API Location"))
                .andExpect(jsonPath("$.totalCapacity").value(200))
                .andExpect(jsonPath("$.occupiedSpaces").value(50));
    }

    @Test
    @DisplayName("POST /api/v1/locations with occupancy > capacity should return 400")
    void createLocationWithInvalidOccupancyReturns400() throws Exception {
        ParkingLocation invalid = new ParkingLocation(
                "Bad Location", "Area", "Addr", 50, 100); // 100 > 50

        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/locations/{id} should return the location")
    void getLocationByIdReturnsCorrectLocation() throws Exception {
        ParkingLocation saved = locationService.save(
                new ParkingLocation("GetById Test", "Area", "Addr", 60, 20));

        mockMvc.perform(get("/api/v1/locations/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("GetById Test"));
    }

    @Test
    @DisplayName("GET /api/v1/locations/99999 should return 404")
    void getLocationByUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/locations/99999"))
                .andExpect(status().isNotFound());
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/alerts should return alerts array")
    void getAlertsReturnsArray() throws Exception {
        // Create a full location to generate an alert
        locationService.save(new ParkingLocation("Alert Trigger", "Area", "Addr", 30, 30));

        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── Update Location ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/locations/{id} should update and return the location")
    void updateLocationReturnsUpdated() throws Exception {
        ParkingLocation saved = locationService.save(
                new ParkingLocation("Original Name", "Area", "Addr", 100, 40));

        ParkingLocation update = new ParkingLocation(
                "Updated Name", "New Area", "New Addr", 100, 60);

        mockMvc.perform(put("/api/v1/locations/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.occupiedSpaces").value(60));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/locations/{id} should return 204")
    void deleteLocationReturns204() throws Exception {
        ParkingLocation saved = locationService.save(
                new ParkingLocation("To Delete", "Area", "Addr", 50, 10));

        mockMvc.perform(delete("/api/v1/locations/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/v1/locations/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    // ── Data Export & Event History ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/locations/export should return 200 with text/csv")
    void exportLocationsCsvReturns200WithCsvHeader() throws Exception {
        locationService.save(new ParkingLocation("Export Test Lot", "South Ward", "100 Gateway", 120, 45));

        mockMvc.perform(get("/api/v1/locations/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("ID,LocationCode,Name,Area,Address,TotalCapacity,OccupiedSpaces,AvailableSpaces,UtilizationPercentage,Status,LastUpdated")))
                .andExpect(content().string(containsString("Export Test Lot")));
    }

    @Test
    @DisplayName("GET /api/v1/events/recent should return 200 without lazy proxy serialization errors")
    void getRecentEventsReturns200WithJsonArray() throws Exception {
        ParkingLocation saved = locationService.save(
                new ParkingLocation("Event Lot", "Central Ward", "200 Metro Way", 100, 20));

        ParkingEventFormDto formDto = new ParkingEventFormDto();
        formDto.setParkingLocationId(saved.getId());
        formDto.setEventType(ParkingEvent.EventType.OCCUPANCY_UPDATE);
        formDto.setReportedOccupancy(35);
        formDto.setDescription("Test Sensor Check");
        formDto.setOperatorName("Daemon Test");
        eventService.recordEvent(formDto);

        mockMvc.perform(get("/api/v1/events/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reportedOccupancy").isNumber())
                .andExpect(jsonPath("$[0].parkingLocation.name").value("Event Lot"));
    }

    @Test
    @DisplayName("GET /api/v1/events/location/{id} should return 200 with location events array")
    void getEventsByLocationReturns200WithJsonArray() throws Exception {
        ParkingLocation saved = locationService.save(
                new ParkingLocation("Location Specific Lot", "North Ward", "300 Highway", 80, 10));

        ParkingEventFormDto formDto = new ParkingEventFormDto();
        formDto.setParkingLocationId(saved.getId());
        formDto.setEventType(ParkingEvent.EventType.MANUAL_CORRECTION);
        formDto.setReportedOccupancy(25);
        formDto.setDescription("Manual Count Verification");
        formDto.setOperatorName("Officer Test");
        eventService.recordEvent(formDto);

        mockMvc.perform(get("/api/v1/events/location/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reportedOccupancy").value(25))
                .andExpect(jsonPath("$[0].eventType").value("MANUAL_CORRECTION"));
    }
}
