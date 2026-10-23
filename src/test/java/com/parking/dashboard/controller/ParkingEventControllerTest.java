package com.parking.dashboard.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ParkingEventController (Web Thymeleaf views).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ParkingEventController Web Integration Tests")
class ParkingEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingLocationService locationService;

    @Autowired
    private ParkingEventService eventService;

    @Test
    @DisplayName("GET /events should render global event audit log with 200")
    void globalEventsListRendersSuccessfully() throws Exception {
        ParkingLocation loc = locationService.save(
                new ParkingLocation("Audit Web Test Lot", "West Ward", "500 Main St", 150, 50));

        ParkingEventFormDto dto = new ParkingEventFormDto();
        dto.setParkingLocationId(loc.getId());
        dto.setEventType(ParkingEvent.EventType.OCCUPANCY_UPDATE);
        dto.setReportedOccupancy(60);
        dto.setDescription("Web view audit test");
        dto.setOperatorName("Tester");
        eventService.recordEvent(dto);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(content().string(containsString("Global Parking Event Audit Log")))
                .andExpect(content().string(containsString("Audit Web Test Lot")))
                .andExpect(content().string(containsString("Web view audit test")));
    }

    @Test
    @DisplayName("GET /events/new should render event recording form with 200")
    void newEventFormRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("event-form"))
                .andExpect(content().string(containsString("Record Parking Update")));
    }

    @Test
    @DisplayName("GET /events/location/{id} should render location events with 200")
    void locationEventsRendersSuccessfully() throws Exception {
        ParkingLocation loc = locationService.save(
                new ParkingLocation("Single Loc Events", "East Ward", "600 Side Rd", 100, 20));

        mockMvc.perform(get("/events/location/" + loc.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("event-list"))
                .andExpect(content().string(containsString("Single Loc Events")));
    }
}
