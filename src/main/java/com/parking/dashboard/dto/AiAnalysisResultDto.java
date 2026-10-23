package com.parking.dashboard.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing the output of an AI parking stall detection run.
 * Strictly decoupled from municipal database models (ParkingLocation, ParkingEvent).
 */
public class AiAnalysisResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;               // "SUCCESS" or "ERROR"
    private int totalDetected;
    private int emptyCount;
    private int occupiedCount;
    private double occupancyPct;
    private double minConf;
    private double maxConf;
    private double avgConf;
    private String resultImageFilename;  // Sanitized unique filename for UI rendering
    private String errorMessage;
    private String disclaimer;
    private long executionTimeMs;
    private double requestedConfidence;
    private boolean reliable = true;
    private String reliabilityWarning;
    private int vehiclesDetected;
    private String calibrationMode; // "CALIBRATED", "PKLOT_SURVEILLANCE", "UNCALIBRATED"
    private Long historyId;
    private String originalImageFilename;

    public AiAnalysisResultDto() {
        this.status = "SUCCESS";
        this.disclaimer = "Academic AI Demonstration — Experimental Model (PKLot Brazil). Not live municipal parking data.";
    }

    public static AiAnalysisResultDto error(String message) {
        AiAnalysisResultDto dto = new AiAnalysisResultDto();
        dto.setStatus("ERROR");
        dto.setErrorMessage(message);
        return dto;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDetected() {
        return totalDetected;
    }

    public void setTotalDetected(int totalDetected) {
        this.totalDetected = totalDetected;
    }

    public int getEmptyCount() {
        return emptyCount;
    }

    public void setEmptyCount(int emptyCount) {
        this.emptyCount = emptyCount;
    }

    public int getOccupiedCount() {
        return occupiedCount;
    }

    public void setOccupiedCount(int occupiedCount) {
        this.occupiedCount = occupiedCount;
    }

    public double getOccupancyPct() {
        return occupancyPct;
    }

    public void setOccupancyPct(double occupancyPct) {
        this.occupancyPct = occupancyPct;
    }

    public double getMinConf() {
        return minConf;
    }

    public void setMinConf(double minConf) {
        this.minConf = minConf;
    }

    public double getMaxConf() {
        return maxConf;
    }

    public void setMaxConf(double maxConf) {
        this.maxConf = maxConf;
    }

    public double getAvgConf() {
        return avgConf;
    }

    public void setAvgConf(double avgConf) {
        this.avgConf = avgConf;
    }

    public String getResultImageFilename() {
        return resultImageFilename;
    }

    public void setResultImageFilename(String resultImageFilename) {
        this.resultImageFilename = resultImageFilename;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public double getRequestedConfidence() {
        return requestedConfidence;
    }

    public void setRequestedConfidence(double requestedConfidence) {
        this.requestedConfidence = requestedConfidence;
    }

    public boolean isReliable() {
        return reliable;
    }

    public void setReliable(boolean reliable) {
        this.reliable = reliable;
    }

    public String getReliabilityWarning() {
        return reliabilityWarning;
    }

    public void setReliabilityWarning(String reliabilityWarning) {
        this.reliabilityWarning = reliabilityWarning;
    }

    public int getVehiclesDetected() {
        return vehiclesDetected;
    }

    public void setVehiclesDetected(int vehiclesDetected) {
        this.vehiclesDetected = vehiclesDetected;
    }

    public String getCalibrationMode() {
        return calibrationMode;
    }

    public void setCalibrationMode(String calibrationMode) {
        this.calibrationMode = calibrationMode;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getOriginalImageFilename() {
        return originalImageFilename;
    }

    public void setOriginalImageFilename(String originalImageFilename) {
        this.originalImageFilename = originalImageFilename;
    }
}
