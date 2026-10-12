package com.parking.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entity representing a parking availability event / update record.
 * Each event captures a snapshot of occupancy at a point in time.
 */
@Entity
@Table(name = "parking_event")
public class ParkingEvent {

    public enum EventType {
        OCCUPANCY_UPDATE,
        LOCATION_OPENED,
        LOCATION_CLOSED,
        MAINTENANCE_START,
        MAINTENANCE_END,
        ALERT_GENERATED,
        MANUAL_CORRECTION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Parking location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_location_id", nullable = false)
    private ParkingLocation parkingLocation;

    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    @NotNull(message = "Reported occupancy is required")
    @Min(value = 0, message = "Occupancy cannot be negative")
    @Column(nullable = false)
    private Integer reportedOccupancy;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    @Column(length = 500)
    private String description;

    @Size(max = 100, message = "Operator name must be 100 characters or fewer")
    @Column(length = 100)
    private String operatorName;

    /** Previous occupancy value before this event */
    @Column
    private Integer previousOccupancy;

    // ==================== Constructors ====================

    public ParkingEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public ParkingEvent(ParkingLocation location, EventType type, Integer occupancy,
                        String description, String operator) {
        this.parkingLocation = location;
        this.eventType = type;
        this.reportedOccupancy = occupancy;
        this.description = description;
        this.operatorName = operator;
        this.eventTimestamp = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParkingLocation getParkingLocation() { return parkingLocation; }
    public void setParkingLocation(ParkingLocation parkingLocation) { this.parkingLocation = parkingLocation; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Integer getReportedOccupancy() { return reportedOccupancy; }
    public void setReportedOccupancy(Integer reportedOccupancy) { this.reportedOccupancy = reportedOccupancy; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public Integer getPreviousOccupancy() { return previousOccupancy; }
    public void setPreviousOccupancy(Integer previousOccupancy) { this.previousOccupancy = previousOccupancy; }

    @Override
    public String toString() {
        return "ParkingEvent{id=" + id + ", type=" + eventType +
               ", occupancy=" + reportedOccupancy + ", ts=" + eventTimestamp + "}";
    }
}
