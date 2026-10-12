package com.parking.dashboard.controller;

import com.parking.dashboard.config.ParkingConfig;
import com.parking.dashboard.dto.DashboardSummaryDto;
import com.parking.dashboard.dto.ParkingAlertDto;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.service.ParkingLocationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web (Thymeleaf) controller for dashboard and parking location pages.
 */
@Controller
public class DashboardController {

    private final ParkingLocationService locationService;
    private final ParkingConfig config;

    public DashboardController(ParkingLocationService locationService, ParkingConfig config) {
        this.locationService = locationService;
        this.config = config;
    }

    /** Main dashboard page */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String filter,
                            Model model) {
        DashboardSummaryDto summary = locationService.getDashboardSummary();
        List<ParkingLocation> locations;

        if (search != null && !search.isBlank()) {
            locations = locationService.search(search);
            model.addAttribute("activeSearch", search);
        } else if (filter != null && !filter.isBlank()) {
            locations = locationService.filterByStatus(filter);
            model.addAttribute("activeFilter", filter);
        } else {
            locations = locationService.findAll();
        }

        model.addAttribute("summary", summary);
        model.addAttribute("locations", locations);
        model.addAttribute("refreshSeconds", config.getDashboardRefreshSeconds());
        model.addAttribute("pageTitle", "Parking Dashboard");
        return "dashboard";
    }

    /** Parking location detail page */
    @GetMapping("/locations/{id}")
    public String locationDetail(@PathVariable Long id, Model model) {
        ParkingLocation location = locationService.findById(id);
        model.addAttribute("location", location);
        model.addAttribute("pageTitle", location.getName());
        return "location-detail";
    }

    /** Add new parking location form */
    @GetMapping("/locations/new")
    public String newLocationForm(Model model) {
        model.addAttribute("location", new ParkingLocation());
        model.addAttribute("pageTitle", "Add Parking Location");
        model.addAttribute("formAction", "/locations");
        model.addAttribute("submitLabel", "Create Location");
        return "location-form";
    }

    /** Edit parking location form */
    @GetMapping("/locations/{id}/edit")
    public String editLocationForm(@PathVariable Long id, Model model) {
        ParkingLocation location = locationService.findById(id);
        model.addAttribute("location", location);
        model.addAttribute("pageTitle", "Edit - " + location.getName());
        model.addAttribute("formAction", "/locations/" + id + "/edit");
        model.addAttribute("submitLabel", "Save Changes");
        return "location-form";
    }

    /** Handle new location form submission */
    @PostMapping("/locations")
    public String createLocation(@ModelAttribute ParkingLocation location,
                                  org.springframework.validation.BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Parking Location");
            model.addAttribute("formAction", "/locations");
            model.addAttribute("submitLabel", "Create Location");
            return "location-form";
        }
        ParkingLocation saved = locationService.save(location);
        return "redirect:/locations/" + saved.getId() + "?success=created";
    }

    /** Handle edit form submission */
    @PostMapping("/locations/{id}/edit")
    public String updateLocation(@PathVariable Long id,
                                  @ModelAttribute ParkingLocation location,
                                  org.springframework.validation.BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Location");
            model.addAttribute("formAction", "/locations/" + id + "/edit");
            model.addAttribute("submitLabel", "Save Changes");
            return "location-form";
        }
        locationService.update(id, location);
        return "redirect:/locations/" + id + "?success=updated";
    }

    /** Delete (soft) a parking location */
    @PostMapping("/locations/{id}/delete")
    public String deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
        return "redirect:/?success=deleted";
    }

    /** Alerts page */
    @GetMapping("/alerts")
    public String alertsPage(Model model) {
        List<ParkingAlertDto> alerts = locationService.getAlerts();
        long criticalCount = alerts.stream()
                .filter(a -> a.getSeverity() == ParkingAlertDto.Severity.CRITICAL).count();
        long highCount = alerts.stream()
                .filter(a -> a.getSeverity() == ParkingAlertDto.Severity.HIGH).count();
        long mediumCount = alerts.stream()
                .filter(a -> a.getSeverity() == ParkingAlertDto.Severity.MEDIUM).count();

        model.addAttribute("alerts", alerts);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("highCount", highCount);
        model.addAttribute("mediumCount", mediumCount);
        model.addAttribute("totalAlerts", alerts.size());
        model.addAttribute("pageTitle", "Alerts & Exceptions");
        return "alerts";
    }

    /** About page */
    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("pageTitle", "About");
        return "about";
    }
}
