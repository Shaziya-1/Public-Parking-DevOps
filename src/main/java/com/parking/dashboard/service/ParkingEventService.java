package com.parking.dashboard.service;

import com.parking.dashboard.dto.ParkingEventFormDto;
import com.parking.dashboard.exception.InvalidParkingUpdateException;
import com.parking.dashboard.exception.ParkingLocationNotFoundException;
import com.parking.dashboard.model.ParkingEvent;
import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.repository.ParkingEventRepository;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for parking event business logic.
 * Handles event recording, duplicate prevention, and occupancy updates.
 */
@Service
@Transactional
public class ParkingEventService {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventService.class);

    /** Duplicate-detection window: events within 10 seconds with same params are rejected. */
    private static final int DUPLICATE_WINDOW_SECONDS = 10;

    private final ParkingEventRepository eventRepo;
    private final ParkingLocationRepository locationRepo;

    public ParkingEventService(ParkingEventRepository eventRepo,
                                ParkingLocationRepository locationRepo) {
        this.eventRepo = eventRepo;
        this.locationRepo = locationRepo;
    }

    /**
     * Record a new parking event and update the location occupancy accordingly.
     *
     * @param formDto validated event form data
     * @return the saved ParkingEvent
     */
    public ParkingEvent recordEvent(ParkingEventFormDto formDto) {
        ParkingLocation location = locationRepo.findById(formDto.getParkingLocationId())
                .orElseThrow(() -> new ParkingLocationNotFoundException(formDto.getParkingLocationId()));

        // Business rule: occupancy cannot exceed capacity
        if (formDto.getReportedOccupancy() > location.getTotalCapacity()) {
            throw new InvalidParkingUpdateException(
                "Reported occupancy (" + formDto.getReportedOccupancy() +
                ") exceeds capacity (" + location.getTotalCapacity() + ") " +
                "for location: " + location.getName());
        }

        // Business rule: occupancy cannot be negative
        if (formDto.getReportedOccupancy() < 0) {
            throw new InvalidParkingUpdateException("Occupancy cannot be negative.");
        }

        // Duplicate detection: prevent accidental re-submission
        LocalDateTime dedupeWindow = LocalDateTime.now().minusSeconds(DUPLICATE_WINDOW_SECONDS);
        boolean isDuplicate = eventRepo.existsDuplicateEvent(
                location.getId(),
                formDto.getEventType(),
                formDto.getReportedOccupancy(),
                dedupeWindow);

        if (isDuplicate) {
            throw new InvalidParkingUpdateException(
                "A duplicate event was detected. Please wait before re-submitting the same update.");
        }

        // Create and persist the event
        ParkingEvent event = new ParkingEvent();
        event.setParkingLocation(location);
        event.setEventType(formDto.getEventType());
        event.setPreviousOccupancy(location.getOccupiedSpaces());
        event.setReportedOccupancy(formDto.getReportedOccupancy());
        event.setDescription(formDto.getDescription());
        event.setOperatorName(formDto.getOperatorName());
        event.setEventTimestamp(LocalDateTime.now());

        ParkingEvent saved = eventRepo.save(event);

        // Update the location's current occupancy (for OCCUPANCY_UPDATE and MANUAL_CORRECTION)
        if (formDto.getEventType() == ParkingEvent.EventType.OCCUPANCY_UPDATE ||
            formDto.getEventType() == ParkingEvent.EventType.MANUAL_CORRECTION) {
            location.setOccupiedSpaces(formDto.getReportedOccupancy());
            location.setLastUpdated(LocalDateTime.now());
            locationRepo.save(location);
            log.info("Updated occupancy for '{}' to {}", location.getName(), formDto.getReportedOccupancy());
        }

        log.info("Recorded event {} for location '{}' (occupancy: {})",
                 saved.getId(), location.getName(), formDto.getReportedOccupancy());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ParkingEvent> findEventsForLocation(Long locationId) {
        return eventRepo.findByParkingLocationIdOrderByEventTimestampDesc(locationId);
    }

    @Transactional(readOnly = true)
    public List<ParkingEvent> findRecentEvents(int limit) {
        return eventRepo.findRecentEvents(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public ParkingEvent findById(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new ParkingLocationNotFoundException("Event not found: " + id));
    }
}
