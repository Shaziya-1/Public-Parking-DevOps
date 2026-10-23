package com.parking.dashboard.controller;

import com.parking.dashboard.dto.ParkingEventFormDto;
import com.parking.dashboard.model.ParkingEvent;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.service.ParkingEventService;
import com.parking.dashboard.service.ParkingLocationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Web (Thymeleaf) controller for parking event entry and listing.
 */
@Controller
@RequestMapping("/events")
public class ParkingEventController {

    private final ParkingEventService eventService;
    private final ParkingLocationService locationService;

    public ParkingEventController(ParkingEventService eventService,
                                   ParkingLocationService locationService) {
        this.eventService = eventService;
        this.locationService = locationService;
    }

    /** Event entry form page */
    @GetMapping("/new")
    public String newEventForm(@RequestParam(required = false) Long locationId, Model model) {
        List<ParkingLocation> locations = locationService.findAll();
        ParkingEventFormDto formDto = new ParkingEventFormDto();
        if (locationId != null) {
            formDto.setParkingLocationId(locationId);
        }
        model.addAttribute("eventForm", formDto);
        model.addAttribute("locations", locations);
        model.addAttribute("eventTypes", ParkingEvent.EventType.values());
        model.addAttribute("pageTitle", "Record Parking Update");
        return "event-form";
    }

    /** Handle event form submission */
    @PostMapping
    public String submitEvent(@Valid @ModelAttribute("eventForm") ParkingEventFormDto formDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("locations", locationService.findAll());
            model.addAttribute("eventTypes", ParkingEvent.EventType.values());
            model.addAttribute("pageTitle", "Record Parking Update");
            return "event-form";
        }

        try {
            ParkingEvent saved = eventService.recordEvent(formDto);
            redirectAttributes.addFlashAttribute("successMessage",
                "Update recorded successfully (Event ID: " + saved.getId() + ").");
            return "redirect:/locations/" + formDto.getParkingLocationId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("locations", locationService.findAll());
            model.addAttribute("eventTypes", ParkingEvent.EventType.values());
            model.addAttribute("pageTitle", "Record Parking Update");
            return "event-form";
        }
    }

    /** Events list for a specific location */
    @GetMapping("/location/{locationId}")
    public String eventsByLocation(@PathVariable Long locationId, Model model) {
        ParkingLocation location = locationService.findById(locationId);
        List<ParkingEvent> events = eventService.findEventsForLocation(locationId);
        model.addAttribute("location", location);
        model.addAttribute("events", events);
        model.addAttribute("pageTitle", "Events - " + location.getName());
        return "event-list";
    }

    /** Recent events list across all locations (Global Audit Log) */
    @GetMapping
    public String allRecentEvents(@RequestParam(defaultValue = "50") int limit, Model model) {
        List<ParkingEvent> events = eventService.findRecentEvents(Math.min(limit, 100));
        model.addAttribute("events", events);
        model.addAttribute("pageTitle", "Parking Event Audit Log");
        return "event-list";
    }
}
