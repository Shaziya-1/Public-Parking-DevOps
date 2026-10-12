package com.parking.dashboard.dto;

import java.time.LocalDateTime;

/**
 * DTO for parking alerts and exceptions.
 */
public class ParkingAlertDto {

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum AlertType { FULL, LOW_AVAILABILITY, STALE_DATA, OVER_CAPACITY }

    private Long locationId;
    private String locationName;
    private String area;
    private AlertType alertType;
    private Severity severity;
    private String reason;
    private LocalDateTime detectedAt;
    private int availableSpaces;
    private int totalCapacity;
    private double utilizationPercentage;

    public ParkingAlertDto() {
        this.detectedAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public int getAvailableSpaces() { return availableSpaces; }
    public void setAvailableSpaces(int availableSpaces) { this.availableSpaces = availableSpaces; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public double getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(double utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}
