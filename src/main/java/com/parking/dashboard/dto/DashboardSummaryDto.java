package com.parking.dashboard.dto;

/**
 * DTO for dashboard summary statistics.
 * All values are computed from live database records.
 */
public class DashboardSummaryDto {

    private long totalLocations;
    private long totalCapacity;
    private long totalOccupied;
    private long totalAvailable;
    private double overallUtilizationPercentage;
    private long fullLocations;
    private long lowAvailabilityLocations;
    private long staleLocations;
    private int lowAvailabilityThreshold;

    public DashboardSummaryDto() {}

    // ==================== Getters & Setters ====================

    public long getTotalLocations() { return totalLocations; }
    public void setTotalLocations(long totalLocations) { this.totalLocations = totalLocations; }

    public long getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(long totalCapacity) { this.totalCapacity = totalCapacity; }

    public long getTotalOccupied() { return totalOccupied; }
    public void setTotalOccupied(long totalOccupied) { this.totalOccupied = totalOccupied; }

    public long getTotalAvailable() { return totalAvailable; }
    public void setTotalAvailable(long totalAvailable) { this.totalAvailable = totalAvailable; }

    public double getOverallUtilizationPercentage() { return overallUtilizationPercentage; }
    public void setOverallUtilizationPercentage(double overallUtilizationPercentage) {
        this.overallUtilizationPercentage = overallUtilizationPercentage;
    }

    public long getFullLocations() { return fullLocations; }
    public void setFullLocations(long fullLocations) { this.fullLocations = fullLocations; }

    public long getLowAvailabilityLocations() { return lowAvailabilityLocations; }
    public void setLowAvailabilityLocations(long lowAvailabilityLocations) {
        this.lowAvailabilityLocations = lowAvailabilityLocations;
    }

    public long getStaleLocations() { return staleLocations; }
    public void setStaleLocations(long staleLocations) { this.staleLocations = staleLocations; }

    public int getLowAvailabilityThreshold() { return lowAvailabilityThreshold; }
    public void setLowAvailabilityThreshold(int lowAvailabilityThreshold) {
        this.lowAvailabilityThreshold = lowAvailabilityThreshold;
    }
}
