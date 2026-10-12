package com.parking.dashboard.dto;

import com.parking.dashboard.model.ParkingEvent;
import jakarta.validation.constraints.*;

/**
 * DTO / form-backing object for recording a new parking event.
 * Validated before the event is persisted.
 */
public class ParkingEventFormDto {

    @NotNull(message = "Parking location is required")
    private Long parkingLocationId;

    @NotNull(message = "Event type is required")
    private ParkingEvent.EventType eventType;

    @NotNull(message = "Reported occupancy is required")
    @Min(value = 0, message = "Occupancy cannot be negative")
    private Integer reportedOccupancy;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    @Size(max = 100, message = "Operator name must be 100 characters or fewer")
    private String operatorName;

    // ==================== Getters & Setters ====================

    public Long getParkingLocationId() { return parkingLocationId; }
    public void setParkingLocationId(Long parkingLocationId) { this.parkingLocationId = parkingLocationId; }

    public ParkingEvent.EventType getEventType() { return eventType; }
    public void setEventType(ParkingEvent.EventType eventType) { this.eventType = eventType; }

    public Integer getReportedOccupancy() { return reportedOccupancy; }
    public void setReportedOccupancy(Integer reportedOccupancy) {
        this.reportedOccupancy = reportedOccupancy;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
}
