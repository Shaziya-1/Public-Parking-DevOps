package com.parking.dashboard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entity representing a public parking location.
 * Contains capacity, occupancy, and metadata for the parking facility.
 */
@Entity
@Table(name = "parking_location")
public class ParkingLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Parking location name is required")
    @Size(max = 150, message = "Name must be 150 characters or fewer")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Area/locality is required")
    @Size(max = 100, message = "Area must be 100 characters or fewer")
    @Column(nullable = false, length = 100)
    private String area;

    @Size(max = 255, message = "Address must be 255 characters or fewer")
    @Column(length = 255)
    private String address;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    @Column(nullable = false)
    private Integer totalCapacity;

    @NotNull(message = "Occupied spaces value is required")
    @Min(value = 0, message = "Occupied spaces cannot be negative")
    @Column(nullable = false)
    private Integer occupiedSpaces;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(length = 50)
    private String locationCode;

    @Column(length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    // ==================== Derived / Calculated Methods ====================

    /**
     * Available spaces = total capacity - occupied spaces.
     * Guarded to never return negative.
     */
    @Transient
    public int getAvailableSpaces() {
        if (totalCapacity == null || occupiedSpaces == null) return 0;
        return Math.max(0, totalCapacity - occupiedSpaces);
    }

    /**
     * Utilization percentage (0-100).
     */
    @Transient
    public double getUtilizationPercentage() {
        if (totalCapacity == null || totalCapacity == 0) return 0.0;
        double occupied = (occupiedSpaces == null) ? 0 : occupiedSpaces;
        return Math.min(100.0, (occupied / totalCapacity) * 100.0);
    }

    /**
     * Returns true if the location is completely full.
     */
    @Transient
    public boolean isFull() {
        return getAvailableSpaces() == 0;
    }

    /**
     * Returns true if occupiedSpaces > totalCapacity (data anomaly).
     */
    @Transient
    public boolean isOverCapacity() {
        if (totalCapacity == null || occupiedSpaces == null) return false;
        return occupiedSpaces > totalCapacity;
    }

    /**
     * Returns true if lastUpdated is older than the given stale threshold (minutes).
     */
    @Transient
    public boolean isStale(int staleThresholdMinutes) {
        if (lastUpdated == null) return true;
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(staleThresholdMinutes));
    }

    /**
     * Availability status badge label.
     */
    @Transient
    public String getAvailabilityStatus() {
        if (isOverCapacity()) return "OVER_CAPACITY";
        double pct = getUtilizationPercentage();
        if (pct >= 100) return "FULL";
        if (pct >= 85) return "LOW";
        if (pct >= 50) return "MEDIUM";
        return "HIGH";
    }

    // ==================== Constructors ====================

    public ParkingLocation() {
        this.lastUpdated = LocalDateTime.now();
    }

    public ParkingLocation(String name, String area, String address,
                           Integer totalCapacity, Integer occupiedSpaces) {
        this.name = name;
        this.area = area;
        this.address = address;
        this.totalCapacity = totalCapacity;
        this.occupiedSpaces = occupiedSpaces;
        this.lastUpdated = LocalDateTime.now();
        this.active = true;
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getOccupiedSpaces() { return occupiedSpaces; }
    public void setOccupiedSpaces(Integer occupiedSpaces) { this.occupiedSpaces = occupiedSpaces; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "ParkingLocation{id=" + id + ", name='" + name + "', area='" + area +
               "', capacity=" + totalCapacity + ", occupied=" + occupiedSpaces + "}";
    }
}
